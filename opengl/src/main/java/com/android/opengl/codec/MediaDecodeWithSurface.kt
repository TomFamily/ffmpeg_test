package com.android.opengl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

/**
 * 解码编码后的码流，将surface作为输出
 */
internal class MediaDecodeWithSurface(
    private val inputH264BufferQueue: LinkedBlockingQueue<Pair<ByteBuffer, MediaCodec.BufferInfo>>,
    val surface: Surface,
    private val threadSleepDuration: Long,
    sps: ByteArray,
    pps: ByteArray,
    private val videoType: String,
    private val videoSize: Pair<Int, Int>,
    private val videoFps: Int
) {

    private lateinit var mediaDeCodec: MediaCodec
    private var encoderThread: HandlerThread? = null
    private var encoderHandler: Handler? = null
    private val inputBufferIndex = LinkedBlockingQueue<Int>()

    private val codecCallback by lazy {
        object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (PRINT_FREQUENT_LOG) Log.d(TAG, "onInputBufferAvailable: $index")
                if (!inputBufferIndex.contains(index)) inputBufferIndex.add(index)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec, index: Int, info: MediaCodec.BufferInfo
            ) {
                if (PRINT_FREQUENT_LOG) Log.d(TAG, "onOutputBufferAvailable: $index")
                mediaDeCodec.releaseOutputBuffer(index, true)
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.d(TAG, "onError:${e.errorCode} ${e.message}")
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Log.d(TAG, "onOutputFormatChanged format: $format")
            }
        }
    }

    init {
        initMediaCodec(sps = sps, pps = pps)
        startProgress()
    }

    private var progressThread: Thread? = null
    private fun startProgress() {
        progressThread = Thread {
            while (true) {
                if (inputH264BufferQueue.isEmpty() || inputBufferIndex.isEmpty() || !::mediaDeCodec.isInitialized) {
                    Thread.sleep(threadSleepDuration)
                    // Log.e(TAG, "startProgress: ${inputH264BufferQueue.size} ${inputBufferIndex.size} ${::mediaDeCodec.isInitialized}")
                } else {
                    val inputIndex = inputBufferIndex.poll()!!
                    val data = inputH264BufferQueue.poll()!!
                    decodeData(data, inputIndex)
                }
            }
        }
        progressThread?.start()
    }

    private fun decodeData(info: Pair<ByteBuffer, MediaCodec.BufferInfo>, inIndex: Int) {
        val byteBuffer = mediaDeCodec.getInputBuffer(inIndex)
        byteBuffer?.put(info.first)
        mediaDeCodec.queueInputBuffer(
            inIndex, 0, info.second.size, info.second.presentationTimeUs, info.second.flags
        )
    }

    private fun initMediaCodec(sps: ByteArray, pps: ByteArray) {
        if (::mediaDeCodec.isInitialized) return

        try {
            if (this.encoderHandler == null) {
                encoderThread = HandlerThread(TAG)
                encoderThread!!.start()
                encoderHandler = Handler(this.encoderThread!!.looper)
            }

            mediaDeCodec = MediaCodec.createDecoderByType(videoType)

            val mediaFormat =
                MediaFormat.createVideoFormat(videoType, videoSize.first, videoSize.second)
                    .also { format ->
                        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible)
                        format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFps)
                        format.setByteBuffer("csd-0", ByteBuffer.wrap(sps))
                        format.setByteBuffer("csd-1", ByteBuffer.wrap(pps))

                        // 这段代码看起来是在设置特定于某个厂商（可能是海思，“hisi” 通常与海思半导体相关）的视频解码格式相关的参数。
                        format.setInteger("vendor.hisi-ext-video-dec-avc.video-scene-for-cloud-pc-req", 1)
                        format.setInteger("vendor.hisi-ext-video-dec-avc.video-scene-for-cloud-pc-rdy", -1)
                    }

            mediaDeCodec.setCallback(codecCallback, encoderHandler)
            mediaDeCodec.configure(mediaFormat, surface, null, 0)
            mediaDeCodec.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseCodec() {
        if (::mediaDeCodec.isInitialized) {
            try {
                mediaDeCodec.flush()
                mediaDeCodec.stop()
                mediaDeCodec.release()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        progressThread?.interrupt()
    }

    private var curFrame = 0
    private fun printDebugLog(
        width: Int,
        height: Int,
        info: MediaCodec.BufferInfo
    ) {
        if (!SAVE_FREQUENT_DEBUG_LOG.first) return

        if (curFrame % SAVE_FREQUENT_DEBUG_LOG.second == 0) {
            Log.d(TAG, "printDebugLog: ${info.size} $width $height $curFrame")
        }
        curFrame++
    }

    companion object {
        private const val TAG = "MediaDecodeWithSurface"
    }
}