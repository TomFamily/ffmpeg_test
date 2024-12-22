package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log

/**
 * 实现 camera1 + opengl 预览
 */
class YGLSurfaceView(context: Context, attributeSet: AttributeSet): GLSurfaceView(context, attributeSet),
    SurfaceTexture.OnFrameAvailableListener {

    init {
        setEGLContextClientVersion(2)
          setRenderer(YGLSurfaceViewRender(context, this))
//        setRenderer(CameraRenderer(context, this))
        renderMode = RENDERMODE_WHEN_DIRTY
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture?) {
        Log.d(TAG, "onFrameAvailable: ")
        requestRender()
    }

    companion object {
        private const val TAG = "YGLSurfaceView"
    }
}