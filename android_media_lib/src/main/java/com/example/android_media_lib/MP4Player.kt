package com.example.android_media_lib

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.SurfaceHolder

class MP4Player: MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val mediaPlayer by lazy { MediaPlayer() }

    val callback = object : SurfaceHolder.Callback {
        override fun surfaceCreated(holder: SurfaceHolder) {
            mediaPlayer.setSurface(holder.surface)
            /**
             * 2、initialized -> prepared
             */
            mediaPlayer.prepareAsync()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

        override fun surfaceDestroyed(holder: SurfaceHolder) {}
    }

    init {
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
    }

    fun start(surfaceHolder: SurfaceHolder, path: String) {
        /**
         * 1、Idle -> initialized
         */
        mediaPlayer.setDataSource(path)
        surfaceHolder.addCallback(callback)
    }

    fun start(context: Context, surfaceHolder: SurfaceHolder, res: Int) {
        /**
         * 1、Idle -> initialized
         */
        mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + res))
        surfaceHolder.addCallback(callback)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        Log.d(TAG, "onPrepared: ")
        mediaPlayer.start()
    }

    override fun onCompletion(mp: MediaPlayer?) {
        Log.d(TAG, "onCompletion: ")
        mediaPlayer.release()
    }

    companion object {
        private const val TAG = "MP4Player"
    }
}