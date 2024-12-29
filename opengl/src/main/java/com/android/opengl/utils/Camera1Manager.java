package com.android.opengl.utils;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import com.android.opengl.codec.CodecManager;

import java.io.IOException;


/**
 * Camera1
 */
public class Camera1Manager {

    private Camera mCamera = null;

    public synchronized void OpenCamera(SurfaceTexture surfaceTexture) {
        try {
            mCamera = Camera.open(CAMERA_FACING_BACK);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");
            // 用在 OpenGL 报错：无法设置
            // parameters.setFlashMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
             parameters.setPreviewSize(1280, 720);
            mCamera.setDisplayOrientation(90);
            mCamera.setParameters(parameters);
            mCamera.setPreviewTexture(surfaceTexture);
            mCamera.setPreviewCallback((data, camera) -> {
                CodecManager.INSTANCE.inputRawVideoData(data);
            });
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private synchronized void closeCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
        }
    }

}
