package com.android.opengl.watermark.orthogonal

import android.content.Context
import android.util.AttributeSet
import com.android.opengl.watermark.egl.YGLSurfaceView

class BitmapOrthogonalSurfaceView(context: Context, attributeSet: AttributeSet): YGLSurfaceView(context, attributeSet) {

    init {
         setRender(BitmapOrthogonalRender(context))
    }
}