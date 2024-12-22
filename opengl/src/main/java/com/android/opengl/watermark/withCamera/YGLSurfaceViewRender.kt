package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.android.opengl.utils.Camera1Manager
import com.android.opengl.watermark.egl.YShaderUtil
import com.example.opengl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * 功能实现： 参照[CameraRenderer]
 */
class YGLSurfaceViewRender(val context: Context, private val listener: OnFrameAvailableListener) : GLSurfaceView.Renderer {
    private var program: Int = -1
    private var vPosition: Int = -1
    private var fPosition: Int = -1

    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer

    private lateinit var bitmap: Bitmap
    private lateinit var textures: IntArray

    private lateinit var surfaceTexture: SurfaceTexture
    private val mCamera1Manager: Camera1Manager = Camera1Manager()

    init {
        val vertexArray = floatArrayOf(
            -1f, -1f,
            -1f, 1f,
            1f, -1f,
            1f, 1f
        )
        vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexArray)
        vertexBuffer.position(0)

        val fragmentArray = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f
        )
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentArray)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

//        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert)
//        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        val vertexSource = YShaderUtil.getRawResource(context, R.raw.vertex_texture)
        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.fragment_texture)

        program = YShaderUtil.createProgram(vertexSource, fragmentSource)

        vPosition = GLES20.glGetAttribLocation(program, "position")
        fPosition = GLES20.glGetAttribLocation(program, "inputTextureCoordinate")

        textures = IntArray(1)
        //生成一个OpenGl纹理
        GLES20.glGenTextures(1, textures, 0)

        //申请纹理存储区域并设置相关参数
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, textures[0])

        // GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glTexParameterf(GLES20.GL_TEXTURE0, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES20.GL_TEXTURE0, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES20.GL_TEXTURE0, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE0, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        surfaceTexture = SurfaceTexture(textures[0])
        surfaceTexture.setOnFrameAvailableListener(listener)

        GLES20.glBindTexture(GLES20.GL_TEXTURE0, 0)

        mCamera1Manager.OpenCamera(surfaceTexture)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(program)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        //申请纹理存储区域并设置相关参数
        GLES20.glBindTexture(GLES20.GL_TEXTURE0, textures[0])

        //从图像流中将纹理图像更新为最近的帧
        surfaceTexture.updateTexImage()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE0, 0)
    }

}
