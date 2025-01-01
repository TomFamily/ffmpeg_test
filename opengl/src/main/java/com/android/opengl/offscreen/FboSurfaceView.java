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
public class FboSurfaceView extends OffscreenSurfaceView {
    private Pair<Integer, Integer> size = new Pair(1080, 720);

    public FboSurfaceView(Context context) {
        this(context, null);
    }

    public FboSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        FboRender yUsedFboRender = new FboRender(context, size.first, size.second);
        setRender(yUsedFboRender);
        this.surfaceCreated();
        this.surfaceChanged(size.first, size.second);
    }
}