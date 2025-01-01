package com.android.opengl.base.egl_offscreen;


import androidx.annotation.IntRange;

/**
 * 参照 surfaceView 的 Callback
 */
public interface OffscreenCallback {
    void surfaceCreated();

    void surfaceChanged(@IntRange(from = 0) int width, @IntRange(from = 0) int height);

    void surfaceDestroyed();
}
