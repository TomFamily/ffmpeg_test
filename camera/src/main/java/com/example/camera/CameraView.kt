package com.example.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.base.config.output_flv
import kotlinx.android.synthetic.main.layout_camera_view.view.*

class CameraView(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet){

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_camera_view, this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        Log.d(TAG, "onAttachedToWindow: ")
        CameraStrong.createBlob(surface_strong_tv_1, output_flv, surface_strong_tv_1.id.toString())
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