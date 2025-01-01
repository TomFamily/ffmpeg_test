package com.android.opengl.watermark.egl;

import android.opengl.GLES20;
import android.util.Log;

public class GLESUtils {
    public static final String TAG = "GLESUtils";

    private GLESUtils() {} // do not instantiate

    /**
     * Checks to see if a GLES error has been raised.
     */
    public static void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            Log.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }
}
