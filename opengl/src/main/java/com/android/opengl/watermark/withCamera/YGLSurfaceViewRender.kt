package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.android.opengl.watermark.egl.YShaderUtil
import com.example.opengl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class YGLSurfaceViewRender(val context: Context) : GLSurfaceView.Renderer {
    private var program: Int = -1
    private var vPosition: Int = -1
    private var fPosition: Int = -1
    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer
    private lateinit var textures: IntArray
    private lateinit var bitmap: Bitmap
    private lateinit var surfaceTexture: SurfaceTexture

    init {
        val vertexArray = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )
        vertexBuffer =
            ByteBuffer.allocateDirect(vertexArray.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(vertexArray)
        vertexBuffer.position(0)

        val fragmentArray = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
        )
        fragmentBuffer =
            ByteBuffer.allocateDirect(fragmentArray.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().put(fragmentArray)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert)
        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        program = YShaderUtil.createProgram(vertexSource, fragmentSource)

        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        fPosition = GLES20.glGetAttribLocation(program, "fPosition")

        //设置文字支持透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])
        surfaceTexture = SurfaceTexture(textures[0])
        if (mOnSurfaceCreateListener != null) {
            mOnSurfaceCreateListener!!.onSurfaceCreate(surfaceTexture, textures[0])
        }

        //设置纹理的环绕方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        //设置纹理的过滤方式
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        if (!::bitmap.isInitialized || bitmap.isRecycled) bitmap = loadBitmap()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 1f, 0f, 1f)

        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0])

        if (!::bitmap.isInitialized || bitmap.isRecycled) bitmap = loadBitmap()
//        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        GLES20.glDisableVertexAttribArray(fPosition)
        GLES20.glDisableVertexAttribArray(vPosition)
    }

    private fun loadBitmap(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher)
    }

    fun setOnSurfaceCreateListener(l: OnSurfaceCreateListener) {
        mOnSurfaceCreateListener = l
    }

    private var mOnSurfaceCreateListener: OnSurfaceCreateListener? = null
    interface OnSurfaceCreateListener {
        fun onSurfaceCreate(surfaceTexture: SurfaceTexture, textureId: Int)
    }

}
