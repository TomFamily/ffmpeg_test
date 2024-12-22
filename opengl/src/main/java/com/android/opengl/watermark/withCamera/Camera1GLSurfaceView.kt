package com.android.opengl.watermark.withCamera

import android.content.Context
import android.util.AttributeSet
import com.android.opengl.watermark.egl.YGLSurfaceView

class Camera1GLSurfaceView(context: Context, attributeSet: AttributeSet): YGLSurfaceView(context, attributeSet) {
    init {
        setRender(Camera1GLRender(context))
    }
}