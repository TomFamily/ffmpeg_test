package com.android.opengl.offscreen;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;

import com.android.opengl.base.egl_offscreen.OffscreenSurfaceView;

/**
 * author : York
 * date   : 2020/12/20 20:35
 * desc   :
 */
public class FBOBitmapSurfaceView extends OffscreenSurfaceView {
    private Pair<Integer, Integer> size = new Pair(1080, 720);

    public FBOBitmapSurfaceView(Context context) {
        this(context, null);
    }

    public FBOBitmapSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        FBOBitmapRender yUsedFboRender = new FBOBitmapRender(context, size.first, size.second);
        setRender(yUsedFboRender);
        this.surfaceCreated();
        this.surfaceChanged(size.first, size.second);
    }
}