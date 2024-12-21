package com.android.watermark.orthogonal

import android.content.Context
import android.util.AttributeSet
import com.android.watermark.bitmap.YBitmapRender
import com.android.watermark.egl.YGLSurfaceView

class BitmapOrthogonalSurfaceView(context: Context, attributeSet: AttributeSet): YGLSurfaceView(context, attributeSet) {

    init {
         setRender(BitmapOrthogonalRender(context))
    }
}