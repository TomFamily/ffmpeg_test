package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.android.opengl.utils.Camera1Manager
import com.android.opengl.watermark.egl.TextureUtils
import com.android.opengl.watermark.egl.ShaderUtil
import com.example.base.floating.OpenGLFloatingImage.loadVideoData
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
    private var smallBitmapTextureId = -1
    private var width = -1
    private var height = -1

    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer

    private lateinit var textures: IntArray
    private var vboId = 0
    private var yCameraFboRender = YCameraFboRender(context)

    private lateinit var surfaceTexture: SurfaceTexture
    private val mCamera1Manager: Camera1Manager = Camera1Manager()
    private val vertexArray = floatArrayOf(
        -1f, -1f,
        -1f, 1f,
        1f, -1f,
        1f, 1f,

        //用来 加一个 图片水印 到左上角
        0f, 0.5f,
        1f, 0.5f,
        0f, 1f,
        1f, 1f,
    )

    private val fragmentArray = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexArray)
        vertexBuffer.position(0)
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentArray.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentArray)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        yCameraFboRender.onCreate()
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)

//        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert)
//        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        val vertexSource = ShaderUtil.getRawResource(context, R.raw.vertex_texture)
        val fragmentSource = ShaderUtil.getRawResource(context, R.raw.fragment_texture)

        program = ShaderUtil.createProgram(vertexSource, fragmentSource)

        vPosition = GLES20.glGetAttribLocation(program, "position")
        fPosition = GLES20.glGetAttribLocation(program, "inputTextureCoordinate")

        //设置支持透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        //<editor-fold desc="VBO">
        val vbo_s = IntArray(1)
        GLES20.glGenBuffers(1, vbo_s, 0)
        vboId = vbo_s[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArray.size * 4 + fragmentArray.size * 4, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexArray.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexArray.size * 4, fragmentArray.size * 4, fragmentBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        //</editor-fold>

        textures = IntArray(1)
        //生成一个OpenGl纹理
        GLES20.glGenTextures(1, textures, 0)

        //申请纹理存储区域并设置相关参数
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])

        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat())
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE)
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE)
        surfaceTexture = SurfaceTexture(textures[0])
        surfaceTexture.setOnFrameAvailableListener(listener)

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        mCamera1Manager.OpenCamera(surfaceTexture)

        smallBitmapTextureId = TextureUtils.createImageTexture(context, R.drawable.ic_launcher)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        this.width = width
        this.height = height
        GLES20.glViewport(0, 0, width, height)
        yCameraFboRender.onChange(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        GLES20.glUseProgram(program)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArray.size * 4)

        GLES20.glActiveTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES)
        //申请纹理存储区域并设置相关参数
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0])

        //从图像流中将纹理图像更新为最近的帧
        surfaceTexture.updateTexImage()
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        loadVideoData()

        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        drawSmallImage()
    }

    private var preTime = -1L
    private fun loadVideoData() {
        val cur = System.currentTimeMillis()
        if (cur - preTime > 500) {
            loadVideoData(width, height)
            preTime = cur
        }
    }

    private fun drawSmallImage() {
//        yCameraFboRender.onDraw(smallBitmapTextureId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 32)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArray.size * 4)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE_2D)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, smallBitmapTextureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }
}
