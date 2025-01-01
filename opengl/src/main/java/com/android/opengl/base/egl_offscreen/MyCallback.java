package com.android.opengl.base.egl_offscreen;


import androidx.annotation.IntRange;

public interface MyCallback {
    void surfaceCreated();

    void surfaceChanged(@IntRange(from = 0) int width, @IntRange(from = 0) int height);

    void surfaceDestroyed();
}
