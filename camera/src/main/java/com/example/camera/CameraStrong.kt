package com.example.camera

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.util.Log
import android.view.Surface
import android.view.TextureView

object CameraStrong {
    private const val TAG = "CameraStrong"
    private val listBlob by lazy { mutableListOf<MediaPlayerBlob>() }

    init {
        Log.d(TAG, "init")
    }

    fun createBlob(textureView: TextureView, path: String, identity: String) {
        listBlob.forEach {
            if (it.identity == identity) {
                it.onRecreate(textureView)
                return
            }
        }

        listBlob.add(MediaPlayerBlob(textureView, path, identity))
    }

    fun onDestroy() {
        listBlob.forEach {
            it.onDestroy()
        }
        listBlob.clear()
    }
}

private class MediaPlayerBlob(val tv: TextureView, path: String, val identity: String): TextureView.SurfaceTextureListener {
    private var surfaceTexture: SurfaceTexture? = null
    private val mediaPlayer by lazy { MediaPlayer() }
    private lateinit var textureView: TextureView

    init {
        Log.d(TAG, "init")
        onRecreate(tv)
        mediaPlayer.setDataSource(path)
        mediaPlayer.setOnPreparedListener {
            mediaPlayer.start()
        }
    }

    fun onRecreate(reTv: TextureView) {
        Log.d(TAG, "onRecreate: ")
        this.textureView = reTv
        textureView.surfaceTextureListener = this

        surfaceTexture?.let { textureView.setSurfaceTexture(it) }
    }

    fun onStop() {
        mediaPlayer.stop()
    }

    fun onDestroy() {
        surfaceTexture = null
        mediaPlayer.release()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureAvailable: ")
        surfaceTexture = surface
        mediaPlayer.setSurface(Surface(surface))
        mediaPlayer.prepareAsync()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceTextureSizeChanged: ")
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        Log.d(TAG, "onSurfaceTextureDestroyed: ")
        return surfaceTexture == null
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Log.d(TAG, "onSurfaceTextureUpdated: ")
    }

    companion object {
        private const val TAG = "VideoBlob"
    }
}