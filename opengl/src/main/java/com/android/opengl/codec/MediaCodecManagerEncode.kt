package com.android.opengl.codec

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue

/**
 * 将元素码流进行编码
 */
internal class MediaCodecManagerEncode(
    private val inputRawBufferQueue: LinkedBlockingQueue<ByteArray>,
    private val threadSleepDuration: Long,
    private val videoType: String,
    private val videoSize: Pair<Int, Int>,
    private val videoFps: Int,
    private val bitRate: Int,
    private val IFrameInterval: Int,
) {
    private val inputBufferIndex = LinkedBlockingQueue<Int>()
    private val codecCallback by lazy {
        object : MediaCodec.Callback() {
            override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                if (PRINT_FREQUENT_LOG) Log.d(TAG, "onInputBufferAvailable: $index")
                if (!inputBufferIndex.contains(index)) inputBufferIndex.add(index)
            }

            override fun onOutputBufferAvailable(
                codec: MediaCodec,
                index: Int,
                info: MediaCodec.BufferInfo
            ) {
                if (PRINT_FREQUENT_LOG) Log.d(TAG, "onOutputBufferAvailable1: $index")
                if (index >= 0) {
                    try {
                        val frameBuffer = codec.getOutputBuffer(index)
                        if (frameBuffer != null) {
                            dealEncodeFrameData(frameBuffer, info)
                            printDebugLog(info)
                        } else {
                            Log.d(TAG, "onOutputBufferAvailable2 frameDataBuf is null")
                        }
                        codec.releaseOutputBuffer(index, false)
                    } catch (e: Exception) {
                        Log.d(TAG, "onOutputBufferAvailable3 mediaCodec err:${e.message} ${e.stackTraceToString()}")
                    }
                }
            }

            override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                Log.d(TAG, "onError:${e.errorCode} ${e.message}")
            }

            override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {
                Log.d(TAG, "onOutputFormatChanged format: $format")
                if (videoType == H264) {
                    val csd0Buffer = format.getByteBuffer("csd-0")
                    val csd1Buffer = format.getByteBuffer("csd-1")
                    mSpsBytes = csd0Buffer?.array()
                    mPpsBytes = csd1Buffer?.array()
                    Log.d(TAG, "onOutputFormatChanged mSpsBytes：${mSpsBytes.contentToString()}")
                    Log.d(TAG, "onOutputFormatChanged mPpsBytes：${mPpsBytes.contentToString()}")
                }
            }
        }
    }

    private val format: MediaFormat
        get() {
            val format = MediaFormat.createVideoFormat(videoType, videoSize.first, videoSize.second)
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, videoFps)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, IFrameInterval)
            // format.setInteger(MediaFormat.KEY_PREPEND_HEADER_TO_SYNC_FRAMES, 0)
            return format
        }

    private var mMediaCodec: MediaCodec? = null
    private var encoderThread: HandlerThread? = null
    private var encoderHandler: Handler? = null

    private var mSpsBytes: ByteArray? = null
    private val mSpsBytesObservable = BehaviorSubject.create<ByteArray>()
    private var mPpsBytes: ByteArray? = null
    private val mPpsBytesObservable = BehaviorSubject.create<ByteArray>()
    private val fpvPreviewSubject = BehaviorSubject.create<Pair<ByteBuffer, MediaCodec.BufferInfo>>()

    @Volatile
    private var started = false
    @Synchronized
    fun startCodec() {
        if (started) {
            Log.d(TAG, "startCodec: started = $started")
            return
        }

        started = true
        initEncoder()
        encodeVideo()
    }

    @Synchronized
    private fun initEncoder() {
        if (mMediaCodec != null) {
            releaseEncoder()
        }

        if (this.encoderHandler == null) {
            encoderThread = HandlerThread(TAG + "Thread")
            encoderThread!!.start()
            encoderHandler = Handler(this.encoderThread!!.looper)
        }

        mMediaCodec = MediaCodec.createEncoderByType(videoType)
        mMediaCodec!!.setCallback(codecCallback, encoderHandler)

        try {
            mMediaCodec!!.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mMediaCodec!!.start()
        } catch (var16: Exception) {
            Log.d(TAG, "initEncoder exception:${var16.message}")
        }
    }

    private var progressThread: Thread? = null
    private fun encodeVideo() {
        progressThread = Thread {
            while (true) {
                if (inputRawBufferQueue.isEmpty() || inputBufferIndex.isEmpty()) {
                    Thread.sleep(threadSleepDuration)
                    if (PRINT_FREQUENT_LOG)Log.e(TAG, "encodeVideo: ${inputRawBufferQueue.size} ${inputBufferIndex.size}")
                } else {
                    val inputIndex = inputBufferIndex.poll()!!
                    val data = inputRawBufferQueue.poll()!!
                    val inputBuffer = mMediaCodec!!.getInputBuffer(inputIndex)
                    inputBuffer?.put(data)
                    mMediaCodec!!.queueInputBuffer(inputIndex, 0, data.size, 0, 0)
                }
            }
        }
        progressThread?.start()
    }

    private fun dealEncodeFrameData(buffer: ByteBuffer, bufferInfo: MediaCodec.BufferInfo) {
        buffer.position(bufferInfo.offset)
        buffer.limit(bufferInfo.size - bufferInfo.offset)
        val data = ByteArray(bufferInfo.size - bufferInfo.offset)
        buffer.get(data)
        if (PRINT_FREQUENT_LOG) Log.d(TAG, "dealEncodeFrameData1：${bufferInfo.size}")

        if (data.size > 5 && data[4].toInt() and 0x1f == 7) {
            // TODO: 完善 获取 sps pps 逻辑
        }
        mPpsBytes?.let { mPpsBytesObservable.onNext(it) }
        mSpsBytes?.let { mSpsBytesObservable.onNext(it) }
        buffer.position(bufferInfo.offset)
        fpvPreviewSubject.onNext(buffer to bufferInfo)
    }

    fun getPreviewDataObserver(): Observable<Pair<ByteBuffer, MediaCodec.BufferInfo>> =
        fpvPreviewSubject.hide()

    fun getFPVPreviewSPS(): ByteArray? = mSpsBytes

    fun getFPVPreviewPPS(): ByteArray? = mPpsBytes

    fun stopProgress() {
        releaseEncoder()
        progressThread?.interrupt()
    }

    @Synchronized
    private fun releaseEncoder() {
        if (mMediaCodec != null) {
            try {
                mMediaCodec!!.flush()
                mMediaCodec!!.stop()
                mMediaCodec!!.release()
            } catch (var8: IllegalStateException) {
                var8.printStackTrace()
            } finally {
                mMediaCodec = null
            }
        }
    }

    private var curFrame = 0
    private fun printDebugLog(info: MediaCodec.BufferInfo) {
        if (!SAVE_FREQUENT_DEBUG_LOG.first) return

        if (curFrame % SAVE_FREQUENT_DEBUG_LOG.second == 0) {
            Log.d(TAG, "printDebugLog: ${info.size} $curFrame")
        }
        curFrame++
    }

    companion object {
        private const val TAG = "FPVPreviewManagerEncode"
    }
}