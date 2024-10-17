package com.example.camera.watermark.bitmap;

import android.content.Context;
import android.util.AttributeSet;

import com.example.camera.watermark.egl.YGLSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 1:42
 * desc   : 绘制图片纹理
 */
public class YBitmapSurfaceView extends YGLSurfaceView {

    public YBitmapSurfaceView(Context context) {
        this(context, null);
    }

    public YBitmapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        YBitmapRender yBitmapRender = new YBitmapRender(context);
        setRender(yBitmapRender);
    }
}
