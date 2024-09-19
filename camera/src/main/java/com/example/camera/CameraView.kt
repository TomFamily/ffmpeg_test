package com.example.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.base.config.video_mp4

/**
 * surface keep 机制
 */
class CameraView(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet){
    private val myTextureView: TextureView
    init {
        LayoutInflater.from(context).inflate(R.layout.layout_camera_view, this, true)
        myTextureView = findViewById(R.id.surface_strong_tv_1)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: ")
        CameraStrong.createBlob(myTextureView, video_mp4, myTextureView.id.toString())
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        Log.d(TAG, "onVisibilityChanged: $visibility")
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        Log.d(TAG, "onDetachedFromWindow: ")
    }
    
    companion object {
        private const val TAG = "CameraView"
    }
}