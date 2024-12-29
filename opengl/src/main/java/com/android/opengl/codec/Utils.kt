package com.android.opengl.codec

import java.nio.ByteBuffer

private const val TAG = "CodecUtils"

fun NV21ToYUV420Planar(nv21Data: ByteArray, width: Int, height: Int): ByteArray {
    val size = width * height
    val yuv420Data = ByteArray(size + size / 2)

    // 提取Y分量（NV21中的Y分量和YUV420Planar中的Y平面顺序一致，直接复制）
    System.arraycopy(nv21Data, 0, yuv420Data, 0, size)

    // 提取U分量（NV21中的U、V分量是交错存储的，这里提取U分量到YUV420Planar的U平面）
    run {
        var i = size + 1
        var j = 0
        while (j < size / 4) {
            yuv420Data[size + j] = nv21Data[i]
            i += 2
            j++
        }
    }

    // 提取V分量（NV21中的U、V分量是交错存储的，这里提取V分量到YUV420Planar的V平面）
    var i = size
    var j = 0
    while (j < size / 4) {
        yuv420Data[size + size / 4 + j] = nv21Data[i]
        i += 2
        j++
    }
    return yuv420Data
}

fun formatNV21ToNV12(nv21: ByteArray, width: Int, height: Int): ByteArray {
    val nv12 = ByteArray(width * height * 3 / 2)
    val frameSize = width * height
    var j = 0
    System.arraycopy(nv21, 0, nv12, 0, frameSize)
    j = 0
    while (j < frameSize / 2) {
        nv12[frameSize + j - 1] = nv21[j + frameSize]
        j += 2
    }
    j = 0
    while (j < frameSize / 2) {
        nv12[frameSize + j] = nv21[j + frameSize - 1]
        j += 2
    }

    return nv12
}

/**
 * PPS 头以特定起始码（0x000001 或 0x00000001）开始
 * eg: 0, 0, 0, 1, 103, 66, -64, 31, -38, -127, 64, 22, -55, -88, 16, 16, 16, 60, 32, 16, -88, 0, 0, 0, 1, 104, -50, 60, -128
 * 其中（0, 0, 0, 1, 104, -50, 60, -128） 为 pps
 */
fun findPpsHeadIndex(frame: ByteArray): Int {
    if (frame.size < 3) return -1

    for (i in 0 until frame.size - 3) {
        if (frame[i].toInt() == 0 && frame[i + 1].toInt() == 0) {
            if (frame[i + 2].toInt() == 0) {
                if (frame[i + 3].toInt() == 1 && frame[i + 4].toInt() and 0x1f == 8) {
                    return i
                }
            } else if (frame[i + 2].toInt() == 1) {
                if (frame[i + 4].toInt() and 0x1f == 8) {
                    return i
                }
            }
        }
    }

    return -1
}

/**
 * NAL 单元是基本的数据单元，用于封装视频数据（如 SPS、PPS、视频帧等），每个 NAL 单元以特定的起始码（0x000001 或 0x00000001）开头。
 */
fun findNextNal(data: ByteArray, start: Int, length: Int): Int {
    if (start >= 0 && length > 0 && start + length <= data.size) {
        var curStatus = 0
        val lastIndex = start + length
        for (i in start until lastIndex) {
            when (data[i].toInt() and 255) {
                0 -> if (curStatus < 2) ++curStatus
                1 -> {
                    if (curStatus == 2) return i - 2
                    curStatus = 0
                }
                else -> curStatus = 0
            }
        }
    }
    return -1
}

fun findH264SpsPps(data: ByteArray): Pair<ByteArray, ByteArray>? {
    if (data.size > 5 && data[4].toInt() and 0x1f == 7) {
        val ppsPosition = findPpsHeadIndex(data)
        var sliceHead = findNextNal(data, ppsPosition + 4, data.size - ppsPosition - 4)
        var ppsLen = data.size - ppsPosition
        if ((sliceHead >= 0)) {
            if (sliceHead > 1 && data[sliceHead - 1].toInt() == 0) {
                sliceHead -= 1
            }
            ppsLen = sliceHead - ppsPosition
        }
        val sps = ByteArray(ppsPosition)
        val pps = ByteArray(ppsLen)
        System.arraycopy(data, 0, sps, 0, sps.size)
        System.arraycopy(data, ppsPosition, pps, 0, pps.size)
        return sps to pps
    }
    return null
}

//fun findNextNal(data: ByteArray, startIndex: Int, searchLength: Int): Int {
//    var index = startIndex
//    var zeroCount = 0
//
//    while (index < startIndex + searchLength - 3) {
//        if (data[index].toInt() == 0x00) {
//            zeroCount++
//            if (zeroCount == 2) {
//                if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x01) {
//                    // 找到0x000001起始码，可能是下一个NAL单元的开始
//                    return index
//                } else if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x00 && data[index + 3].toInt() == 0x01) {
//                    // 找到0x00000001起始码，可能是下一个NAL单元的开始（用于某些特殊情况或扩展格式）
//                    return index + 1
//                }
//            }
//        } else {
//            zeroCount = 0
//        }
//        index++
//    }
//
//    return -1 // 未找到下一个NAL单元
//}

/**
 * 没验证过准确性
 */
fun searchVpsSpsPpsFromH265(csd0byteBuffer: ByteBuffer) {
    var vpsPosition = -1
    var spsPosition = -1
    var ppsPosition = -1
    var contBufferInitiation = 0
    val csdArray: ByteArray = csd0byteBuffer.array()
    for (i in csdArray.indices) {
        if (contBufferInitiation == 3 && csdArray[i].toInt() == 1) {
            if (vpsPosition == -1) {
                vpsPosition = i - 3
            } else if (spsPosition == -1) {
                spsPosition = i - 3
            } else {
                ppsPosition = i - 3
            }
        }
        if (csdArray[i].toInt() == 0) {
            contBufferInitiation++
        } else {
            contBufferInitiation = 0
        }
    }
    val vps = ByteArray(spsPosition)
    val sps = ByteArray(ppsPosition - spsPosition)
    val pps = ByteArray(csdArray.size - ppsPosition)
    for (i in csdArray.indices) {
        if (i < spsPosition) {
            vps[i] = csdArray[i]
        } else if (i < ppsPosition) {
            sps[i - spsPosition] = csdArray[i]
        } else {
            pps[i - ppsPosition] = csdArray[i]
        }
    }
}

