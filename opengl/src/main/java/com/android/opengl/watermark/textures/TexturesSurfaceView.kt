package com.android.opengl.watermark.textures

import android.content.Context
import android.util.AttributeSet
import com.android.opengl.base.egl.YGLSurfaceView

class TexturesSurfaceView(context: Context, attributeSet: AttributeSet): YGLSurfaceView(context, attributeSet) {

    init {
        setRender(TexturesRender(context))
    }
}