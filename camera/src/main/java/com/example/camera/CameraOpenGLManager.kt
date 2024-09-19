package com.example.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.hardware.Camera
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import com.example.base.utils.PermissionUtil
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * camera1 + openGL + GLSurfaceView
 */
class CameraOpenGLManager(private val context: Context, private val mGLSurfaceView: GLSurfaceView) {
    private var mCameraId = -1
    private var mCamera: Camera? = null


    init {
        //配置OpenGL ES，主要是版本设置和设置Renderer，Renderer用于执行OpenGL的绘制
        mGLSurfaceView.setEGLContextClientVersion(2)
        mGLSurfaceView.setRenderer(MyRender())
        mGLSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
        initPermission()
    }

    private fun initPermission() {
        // PermissionUtil.requestSDCardPermission(this@MainActivity, 1002) { }
        PermissionUtil.requestPermission(context as Activity, Manifest.permission.RECORD_AUDIO, 0)
        PermissionUtil.requestPermission(context, Manifest.permission.CAMERA, 0)
    }

    fun openCamera() {
        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        mCamera = Camera.open(mCameraId)
        val parameters = mCamera!!.parameters
        parameters.set("orientation", "portrait");
        parameters.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE;
        parameters.setPreviewSize(1280, 720);
        mCamera!!.setDisplayOrientation(90)
        // setCameraDisplayOrientation(mActivity, mCameraId, mCamera);
        mCamera!!.parameters = parameters
        mGLSurfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                mCamera!!.setPreviewDisplay(holder)
                mCamera!!.startPreview()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) {
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
            }

        })
    }

    class MyRender : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        }

        override fun onDrawFrame(gl: GL10?) {

        }

    }

    companion object {
        private const val TAG = "CameraOpenGLManager"
    }
}