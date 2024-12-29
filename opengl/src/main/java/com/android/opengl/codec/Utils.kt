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
 */
fun findPpsHeadIndex(data: ByteArray): Int {
    var index = 0
    var state = 0
    var zeroCount = 0

    while (index <= data.lastIndex - 3) {
        if (data[index].toInt() == 0x00) {
            zeroCount++
            if (zeroCount == 2) {
                if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x01) {
                    // 找到0x000001起始码，可能是PPS头
                    state = 1
                    zeroCount = 0
                } else if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x00 && data[index + 3].toInt() == 0x01) {
                    // 找到0x00000001起始码，可能是PPS头（用于某些特殊情况或扩展格式）
                    state = 2
                    zeroCount = 0
                }
            }
        } else {
            zeroCount = 0
        }

        if (state == 1 || state == 2) {
            // 检查后续字节是否符合PPS头的格式（这里简单检查长度字段是否合理，实际应用中可能需要更严格的检查）
            val length = ((data[index + state + 1].toInt() and 0xff) shl 8) or (data[index + state + 2].toInt() and 0xff)
            if (length in 1..99999 && index + state + 3 + length <= data.size) {
                // 长度合理且不超出数据范围，认为找到了PPS头
                return index + state
            } else {
                // 长度不合理，继续查找
                state = 0
            }
        }

        index++
    }

    return -1 // 未找到PPS头
}

/**
 * NAL 单元是基本的数据单元，用于封装视频数据（如 SPS、PPS、视频帧等），每个 NAL 单元以特定的起始码（0x000001 或 0x00000001）开头。
 */
fun findNextNal(data: ByteArray, startIndex: Int, searchLength: Int): Int {
    var index = startIndex
    var zeroCount = 0

    while (index < startIndex + searchLength - 3) {
        if (data[index].toInt() == 0x00) {
            zeroCount++
            if (zeroCount == 2) {
                if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x01) {
                    // 找到0x000001起始码，可能是下一个NAL单元的开始
                    return index
                } else if (data[index + 1].toInt() == 0x00 && data[index + 2].toInt() == 0x00 && data[index + 3].toInt() == 0x01) {
                    // 找到0x00000001起始码，可能是下一个NAL单元的开始（用于某些特殊情况或扩展格式）
                    return index + 1
                }
            }
        } else {
            zeroCount = 0
        }
        index++
    }

    return -1 // 未找到下一个NAL单元
}

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

