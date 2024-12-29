package com.android.opengl.codec

import android.media.MediaCodec
import android.view.Surface
import io.reactivex.disposables.CompositeDisposable
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingQueue


object CodecManager {
    private val rawBufferQueue by lazy { LinkedBlockingQueue<ByteArray>() }
    private val mMediaCodecManagerEncode by lazy {
        MediaCodecManagerEncode(
            rawBufferQueue,
            PROGRESS_SLEEP_INTERVAL.first,
            H264,
            VIDEO_SIZE_NORMAL,
            VIDEO_FPS,
            VIDEO_SIZE_NORMAL.first * VIDEO_SIZE_NORMAL.second * 4,
            1
        )
    }
    @Volatile
    private var mMediaDecodeWithSurface: MediaDecodeWithSurface? = null
    private val inputEncodeVideoBufferQueue by lazy { LinkedBlockingQueue<Pair<ByteBuffer, MediaCodec.BufferInfo>>() }
    private val mCompositeDisposable by lazy { CompositeDisposable() }
    private var decodeSurface: WeakReference<Surface>? = null

    init {
        mMediaCodecManagerEncode.startCodec()
        initSubscribe()
    }

    private fun initSubscribe() {
        val initDecode = {
            if (
                mMediaDecodeWithSurface == null &&
                mMediaCodecManagerEncode.getFPVPreviewSPS() != null &&
                mMediaCodecManagerEncode.getFPVPreviewPPS() != null &&
                decodeSurface != null && decodeSurface!!.get() != null
            ) {
                mMediaDecodeWithSurface = MediaDecodeWithSurface(
                    inputEncodeVideoBufferQueue,
                    decodeSurface!!.get()!!,
                    PROGRESS_SLEEP_INTERVAL.first,
                    sps = mMediaCodecManagerEncode.getFPVPreviewSPS()!!,
                    pps = mMediaCodecManagerEncode.getFPVPreviewPPS()!!,
                    H264,
                    VIDEO_SIZE_NORMAL,
                    VIDEO_FPS
                )
            }
        }

        mCompositeDisposable.add(mMediaCodecManagerEncode.getPreviewDataObserver().subscribe {
            if (mMediaDecodeWithSurface == null) initDecode.invoke()
            if (inputEncodeVideoBufferQueue.size >= 3) inputEncodeVideoBufferQueue.poll()
            inputEncodeVideoBufferQueue.offer(it)
        })
    }

    fun initDecode(surface: Surface) {
        decodeSurface = WeakReference(surface)
    }

    fun inputRawVideoData(data: ByteArray) {
        if (rawBufferQueue.size >= 3) rawBufferQueue.poll()
        formatNV21ToNV12(data, VIDEO_SIZE_NORMAL.first, VIDEO_SIZE_NORMAL.second).also {
            rawBufferQueue.offer(it)
        }
    }
}