package com.example.android_media_lib

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.Surface

class MP4Player(surface: Surface):
    MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    private val mediaPlayer by lazy { MediaPlayer() }

    init {
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setSurface(surface)
    }

    fun start(path: String) {
        mediaPlayer.setDataSource(path)
        mediaPlayer.prepareAsync()
    }

    fun start(context: Context, res: Int) {
        mediaPlayer.setDataSource(context, Uri.parse("android.resource://" + context.packageName + "/" + res))
        mediaPlayer.prepareAsync()
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