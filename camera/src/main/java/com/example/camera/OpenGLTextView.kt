package com.example.camera

import android.app.Activity
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.TextureView
import com.android.utils.Utils
import com.example.camera.cameraManager.CameraV1
import com.example.camera.render.CameraV1GLRenderer

/**
 * opengl + TextView + Camera1 实现图像黑白预览
 */
class OpenGLTextView(context: Context, attributeSet: AttributeSet): TextureView(context, attributeSet) {

    private var mCameraId = 0
    private var mCamera: CameraV1? = null
    private var mOESSurfaceTexture: SurfaceTexture? = null
    private var mOESTextureId = -1
    private var mRenderer: CameraV1GLRenderer? = null
    
    private val mSurfaceTextureListener by lazy { 
        object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture, width: Int, height: Int
            ) {
                mOESTextureId = Utils.createOESTextureObject()
                mRenderer!!.init(this@OpenGLTextView, mOESTextureId, context)
                mOESSurfaceTexture = mRenderer!!.initOESTexture()

                val dm = DisplayMetrics()
                mCamera = CameraV1(context as Activity)
                mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
                if (!mCamera!!.openCamera(dm.widthPixels, dm.heightPixels, mCameraId)) {
                    return
                }

                mCamera!!.setPreviewTexture(mOESSurfaceTexture)
                mCamera!!.startPreview()
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture, width: Int, height: Int
            ) = Unit

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                if (mCamera != null) {
                    mCamera!!.stopPreview()
                    mCamera!!.releaseCamera()
                    mCamera = null
                }
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    }
    
    init {
        mRenderer = CameraV1GLRenderer()
        surfaceTextureListener = mSurfaceTextureListener
    }
    
    companion object {
        private const val TAG = "OpenGLTextView"
    }
}