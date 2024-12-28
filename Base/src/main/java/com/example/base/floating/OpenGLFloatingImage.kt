package com.example.base.floating

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import android.widget.ImageView
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

/**
 * 展示 从帧缓冲区（Frame Buffer）中读取像素数据
 */
object OpenGLFloatingImage {
    private var imageView: WeakReference<ImageView>? = null

    fun setImageView(imageView: ImageView) {
        this.imageView = WeakReference(imageView)
    }

    fun setFloatingImage(bitmap: Bitmap) {
        if (imageView == null || imageView!!.get() == null) {
            Log.d(TAG, "setFloatingImage: imageView == null")
        } else {
            imageView!!.get()!!.post {
                imageView!!.get()!!.setImageBitmap(bitmap)
            }
        }
    }

    /**
     * 从帧缓冲区（Frame Buffer）中读取像素数据
     * 注意：glReadPixels 函数的调用会涉及从图形硬件（GPU）到系统内存（CPU）的数据传输，这个过程相对比较耗时
     * 一、尤其是在读取较大区域的像素数据或者频繁调用该函数时，可能会对应用的性能产生明显的影响。
     * 二、需要在 解绑纹理（GLES20.glBindTexture）前调用
     */
    private var mCaptureBuffer: ByteBuffer? = null
    fun loadVideoData(width: Int, height: Int) {
        if (mCaptureBuffer == null) {
            mCaptureBuffer = ByteBuffer.allocateDirect(width * height * 4)
        }

        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mCaptureBuffer)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(mCaptureBuffer!!.array()))
        setFloatingImage(bitmap)
    }

    private const val TAG = "FloatingImage"
}