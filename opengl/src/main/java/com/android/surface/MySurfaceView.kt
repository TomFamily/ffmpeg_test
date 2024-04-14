package com.android.surface

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.view.SurfaceView

class MySurfaceView(context: Context, attributeSet: AttributeSet): SurfaceView(context, attributeSet) {
    init {

    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val lockCanvas = holder.lockCanvas()
        SurfaceTexture(false)
        holder.surface
    }

    companion object {
        private const val TAG = "MySurfaceView"
    }
}