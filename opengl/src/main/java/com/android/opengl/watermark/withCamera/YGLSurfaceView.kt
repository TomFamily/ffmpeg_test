package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.getCameraInfo
import android.hardware.Camera.getNumberOfCameras
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log


class YGLSurfaceView(context: Context, attributeSet: AttributeSet): GLSurfaceView(context, attributeSet) {

    private val render by lazy { YGLSurfaceViewRender(context) }
    private lateinit var surfaceTexture: SurfaceTexture
    private var textureId: Int = -1
    private var mCamera: Camera? = null
    private var mParams: Camera.Parameters? = null

    init {
        setEGLContextClientVersion(2)
        setRenderer(render)
        renderMode = RENDERMODE_WHEN_DIRTY
        mCamera = Camera.open()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        render.setOnSurfaceCreateListener(object : YGLSurfaceViewRender.OnSurfaceCreateListener {
            override fun onSurfaceCreate(surfaceTexture: SurfaceTexture, textureId: Int) {
                this@YGLSurfaceView.surfaceTexture = surfaceTexture
                this@YGLSurfaceView.textureId = textureId
                mCamera!!.setPreviewTexture(surfaceTexture)
                initCamera()
                configSurfaceTexture()
            }
        })
    }

    private fun configSurfaceTexture() {
        this@YGLSurfaceView.surfaceTexture.setOnFrameAvailableListener {
            Log.d(TAG, "configSurfaceTexture: ")
            requestRender()
        }
    }

    fun open(): Camera? {
        val numberOfCameras = getNumberOfCameras()
        val cameraInfo = Camera.CameraInfo()
        for (i in 0 until numberOfCameras) {
            getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing === Camera.CameraInfo.CAMERA_FACING_BACK) {
                return Camera.open()
            }
        }
        return null
    }

    private fun initCamera() {
        if (mCamera != null) {
            mParams = mCamera!!.parameters
            mParams!!.pictureFormat = PixelFormat.JPEG //设置拍照后存储的图片格式
            mCamera!!.setDisplayOrientation(90)
            //设置摄像头为持续自动聚焦模式
            mParams!!.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            mCamera!!.parameters = mParams
            mCamera!!.startPreview() //开启预览
        }
    }

    companion object {
        private const val TAG = "YGLSurfaceView"
    }
}