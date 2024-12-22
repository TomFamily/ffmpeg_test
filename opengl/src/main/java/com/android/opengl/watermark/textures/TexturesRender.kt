package com.android.opengl.watermark.textures

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.android.opengl.watermark.egl.TextureUtils
import com.android.opengl.watermark.egl.YGLSurfaceView
import com.android.opengl.watermark.egl.YShaderUtil
import com.example.opengl.R
import java.lang.System.currentTimeMillis
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.text.SimpleDateFormat
import java.util.*


class TexturesRender(val context: Context): YGLSurfaceView.YGLRender {
    private var program: Int = -1
    private var vPosition: Int = -1
    private var fPosition: Int = -1
    private lateinit var textures: IntArray
    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer
    private lateinit var bitmap: Bitmap
    private var vboId = 0
    private var smallBitmapTextureId = 0
    private var textTextureId = 0
    private val vertexArr = floatArrayOf(
        -1f, -1f,
        1f, -1f,
        -1f, 1f,
        1f, 1f,

        //用来 加一个 图片水印 到左上角
        0f, 0.5f,
        1f, 0.5f,
        0f, 1f,
        1f, 1f,

        //用来 加一个文字水印 到右下角
        0f, -1f,
        1f, -1f,
        0f, -0.8f,
        1f, -0.8f
    )

    private val fragmentArr = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f,
    )

    init {
        vertexBuffer = ByteBuffer.allocateDirect(vertexArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexArr)
        vertexBuffer.position(0)
        fragmentBuffer = ByteBuffer.allocateDirect(vertexArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentArr)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated() {
        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert)
        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        program = YShaderUtil.createProgram(vertexSource, fragmentSource)

        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        fPosition = GLES20.glGetAttribLocation(program, "fPosition")

        //设置文字支持透明
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        //<editor-fold desc="VBO">
        val vbo_s = IntArray(1)
        GLES20.glGenBuffers(1, vbo_s, 0)
        vboId = vbo_s[0]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexArr.size * 4 + fragmentArr.size * 4, null, GLES20.GL_STATIC_DRAW)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexArr.size * 4, vertexBuffer)
        GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, vertexArr.size * 4, fragmentArr.size * 4, fragmentBuffer)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        //</editor-fold>

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

        smallBitmapTextureId = TextureUtils.createImageTexture(context, R.drawable.ic_launcher)

    }

    private fun loadBitmap(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.byg)
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame() {
        drawBg()
        drawSmallImage()
        drawText()
    }

    @Synchronized
    private fun drawBg() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(0f, 0f, 1f, 1f)

        GLES20.glUseProgram(program)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 0)

        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArr.size * 4)

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

    private fun drawSmallImage() {
        //图片水印
        //从VBO中获取图片水印的坐标，并使能
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 32)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArr.size * 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, smallBitmapTextureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private fun drawText() {
        //图片水印
        //从VBO中获取图片水印的坐标，并使能
        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, 64)
        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, vertexArr.size * 4)

        updateTextTexture()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textTextureId)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    private var preTime = -1L
    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private fun updateTextTexture() {
        val curTime = currentTimeMillis()
        if (curTime - preTime > 2000) {
            TextureUtils.deleteTexture(textTextureId)

            val bitmap = TextureUtils.createTextBitmap(
                sdf.format(Date(curTime)), 25, "#000000", "#00000000", 10
            )
            textTextureId = TextureUtils.loadBitmapTexture(bitmap)
            bitmap.recycle()

            preTime = curTime
        }
    }

    @Synchronized
    override fun surfaceDestroyed() {
        if (::bitmap.isInitialized) bitmap.recycle()
    }


}