package com.android.watermark.orthogonal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.opengl.Matrix
import com.android.watermark.egl.YGLSurfaceView
import com.android.watermark.egl.YShaderUtil
import com.example.opengl.R
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * 正交投影显示图片：
 * 正交投影是线性投影的一种特殊情况，即投影线垂直于投影平面
 */
class BitmapOrthogonalRender(val context: Context): YGLSurfaceView.YGLRender {

    private var vertexBuffer: FloatBuffer
    private var fragmentBuffer: FloatBuffer
    private var program: Int = -1
    private var vPosition: Int = -1
    private var fPosition: Int = -1
    private var u_matrix: Int = -1
    private lateinit var textureIds: IntArray
    private lateinit var bitmap: Bitmap
    private val matrix = FloatArray(16)

    init {
        //顶点坐标
        val vertexArr = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f,
        )
        vertexBuffer = ByteBuffer.allocateDirect(vertexArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(vertexArr)
        vertexBuffer.position(0)

        //纹理坐标
        // 在3D场景中，顶点坐标将是三维的，而纹理坐标可能仍然是二维的（对于2D纹理），或者在某些高级用例中，纹理坐标也可以是三维的（对于体积纹理或立方体贴图）
        /**
         * 纹理坐标系统
         * 1、坐标范围：纹理坐标通常在x和y轴上，范围一般是0~1。纹理坐标从左下角（0,0）开始，结束于右上角（1,1）。
         *          但需要注意，这个范围并不是绝对的，根据纹理的格式和设置，坐标范围也可能有所不同。
         * 2、坐标分量：纹理坐标可包含1~4个分量，这些分量通常被称为s、t、r和q坐标（在二维纹理中，通常只使用s和t坐标）。
         *          这些坐标用于指定纹理图像中的哪个纹素（texel）将被用于渲染的顶点。
         */
        val fragmentArr = floatArrayOf(
            0f, 1f,
            1f, 1f,
            0f, 0f,
            1f, 0f,
        )
        fragmentBuffer = ByteBuffer.allocateDirect(fragmentArr.size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(fragmentArr)
        fragmentBuffer.position(0)
    }

    override fun onSurfaceCreated() {
        val vertexSource = YShaderUtil.getRawResource(context, R.raw.screen_vert_matrix)
        val fragmentSource = YShaderUtil.getRawResource(context, R.raw.screen_frag)
        program = YShaderUtil.createProgram(vertexSource, fragmentSource)
        vPosition = GLES20.glGetAttribLocation(program, "vPosition")
        fPosition = GLES20.glGetAttribLocation(program, "fPosition")
        //从渲染程序中得到投影的属性
        u_matrix = GLES20.glGetUniformLocation(program, "u_Matrix")

        textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        bitmap = loadBitmap()
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        /**
         * 设置一个正交投影矩阵（matrix），该矩阵用于在2D渲染中定义视图的裁剪区域和坐标系的变换。
         * 这个过程通常发生在OpenGL ES或类似的图形API中，用于准备渲染前的投影设置。
         * 正交投影矩阵随后会被用于渲染过程中，以确保渲染的图像保持其原始的宽高比，并根据视图的尺寸进行适当的缩放和裁剪。
         * 这对于在不同尺寸的屏幕上保持图像的一致性和比例非常重要。
         * 需要注意的是，这段代码假设渲染的上下文已经正确设置，包括视图的大小（width和height）以及用于渲染的OpenGL ES环境。
         * 此外，matrix数组应该是一个足够大的浮点数组，以存储正交投影矩阵的值（通常是9个元素，对应3x3的矩阵）。
         */
        //只获取一下要加载图片的大小，不加载
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(context.resources, R.drawable.nobb, options)
        val imageHeight = options.outHeight
        val imageWidth = options.outWidth

        if (width > height) {
            Matrix.orthoM(
                matrix,
                0,
                -width / (height / imageHeight * 1f * imageWidth * 1f),
                width / (height / imageHeight * 1f * imageWidth * 1f),
                -1f,
                1f,
                -1f,
                1f
            )
        } else {
            Matrix.orthoM(
                matrix,
                0,
                -1f,
                1f,
                -height / (width / imageWidth * 1f * imageHeight * 1f),
                height / (width / imageWidth * 1f * imageHeight * 1f),
                -1f,
                1f
            )
        }

    }

    override fun onDrawFrame() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glClearColor(1f,0f, 0f, 1f)

        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(u_matrix, 1, false, matrix, 0)

        GLES20.glEnableVertexAttribArray(vPosition)
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 8, vertexBuffer)

        GLES20.glEnableVertexAttribArray(fPosition)
        GLES20.glVertexAttribPointer(fPosition, 2, GLES20.GL_FLOAT, false, 8, fragmentBuffer)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])

        if (!::bitmap.isInitialized || bitmap.isRecycled) {
            bitmap = loadBitmap()
        }
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)

        /**
         * 绘制图形
         * 1：要绘制的图元的类型
         * 2：从当前绑定的顶点数组或缓冲区对象中开始读取顶点的起始索引
         * 3：要使用的顶点数量
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun surfaceDestroyed() {
        if (::bitmap.isInitialized) bitmap.recycle()
    }

    private fun loadBitmap(): Bitmap {
         return BitmapFactory.decodeResource(context.resources, R.drawable.nobb)
    }

    companion object {
        private const val TAG = "BitmapOrthogonalRender"
    }
}