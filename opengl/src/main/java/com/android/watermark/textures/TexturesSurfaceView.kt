package com.android.watermark.textures

import android.content.Context
import android.util.AttributeSet
import com.android.watermark.egl.YGLSurfaceView

class TexturesSurfaceView(context: Context, attributeSet: AttributeSet): YGLSurfaceView(context, attributeSet) {

    init {
        setRender(TexturesRender(context))
    }
}