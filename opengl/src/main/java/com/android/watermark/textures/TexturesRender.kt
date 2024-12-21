package com.android.watermark.textures

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.android.watermark.egl.YGLSurfaceView
import com.android.watermark.egl.YShaderUtil
import com.example.opengl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class TexturesRender(val context: Context): YGLSurfaceView.YGLRender {
    private var program: Int = -1
    private var vPosition: Int = -1
    private var fPosition: Int = -1
    private lateinit var textures: IntArray
    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer
    private lateinit var bitmap: Bitmap

    init {
        val vertexArr = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )

        vertexBuffer = ByteBuffer.allocateDirect(vertexArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexArr)
        vertexBuffer.position(0)

        val fragmentArr = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
        )
        fragmentBuffer = ByteBuffer.allocateDirect(vertexArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentArr)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated() {
        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert)
        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        program = YShaderUtil.createProgram(vertexSource, fragmentSource)

        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        fPosition = GLES20.glGetAttribLocation(program, "fPosition")

        textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        if (!::bitmap.isInitialized || bitmap.isRecycled) bitmap = loadBitmap()
    }

    private fun loadBitmap(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.nobb)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 1f, 1f)

        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        if (!::bitmap.isInitialized || bitmap.isRecycled) bitmap = loadBitmap()
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        /**
         * GLES20.GL_TRIANGLE_STRIP 的工作原理是，它使用每三个连续的顶点来构造一个三角形，
         * 但是与 GL_TRIANGLES 不同的是，GL_TRIANGLE_STRIP 会重用前一个三角形的两个顶点来与下一个新顶点形成下一个三角形
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

    }

    override fun surfaceDestroyed() {
        if (::bitmap.isInitialized) bitmap.recycle()
    }


}