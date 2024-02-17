package com.example.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet

class AirGLSurfaceView
@JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): GLSurfaceView(context, attrs) {

    init {
//        setEGLContextClientVersion(2)
//        setRenderer(AirHockeyRenderer())
    }
}