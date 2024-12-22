package com.android.opengl.watermark.withCamera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.SurfaceTexture
import android.opengl.*
import android.util.Log
import com.android.opengl.utils.Camera1Manager
import com.example.base.poengl.BufferUtil
import com.example.base.poengl.ShaderUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class CameraRenderer(private val mContext: Context, private val listener: SurfaceTexture.OnFrameAvailableListener) : GLSurfaceView.Renderer {

    private val mCamera1Manager: Camera1Manager
    private var mCameraTexture: SurfaceTexture? = null

    private var mProgram = 0
    private var uPosHandle = 0
    private var aTexHandle = 0

    private var filterSecondTextureCoordinateAttribute = 0
    private var filterInputTextureUniform = 0
    private var filterInputTextureUniform2 = 0
    private var mMVPMatrixHandle = 0
    private val mProjectMatrix = FloatArray(16)
    private val mCameraMatrix = FloatArray(16)
    private val mMVPMatrix = FloatArray(16)
    private val mTempMatrix = FloatArray(16)
    private var texture2CoordinatesBuffer: ByteBuffer? = null

    private val mPosCoordinate = floatArrayOf(
        -1f, -1f,
        -1f, 1f,
        1f, -1f,
        1f, 1f
    )

    // 纹理坐标系（数学基础坐标系）
    private val mTexCoordinate = floatArrayOf(
        0f, 1f,
        1f, 1f,
        0f, 0f,
        1f, 0f
    )
    // 纹理坐标系（数学基础坐标系） 纹理 反转 180度
//        private val mTexCoordinate = floatArrayOf(
//            1f, 0f,
//            0f, 0f,
//            1f, 1f,
//            0f, 1f,
//        )
    private var mPosBuffer: FloatBuffer? = null
    private var mTexBuffer: FloatBuffer? = null

    init {
        // 通过将矩阵设置为单位矩阵，可以确保后续的矩阵操作（如乘法或变换）从一个清晰的状态开始，避免之前的数据影响结果
        Matrix.setIdentityM(mProjectMatrix, 0)
        Matrix.setIdentityM(mCameraMatrix, 0)
        Matrix.setIdentityM(mMVPMatrix, 0)
        Matrix.setIdentityM(mTempMatrix, 0)
        mCamera1Manager = Camera1Manager()
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.e(TAG, "onSurfaceCreated() called with: gl = [$gl], config = [$config]")
        // 用于设置清除颜色。该调用用于设置 OpenGL 的背景色，通常在每一帧的开始阶段调用，以确保渲染的背景是指定的颜色。
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 0.0f)
        mProgram =
            ShaderUtils.createProgram(mContext, "vertex_texture.glsl", "fragment_texture.glsl")
        GLES20.glUseProgram(mProgram) //激活OpenGl程序
        createAndBindVideoTexture()
        mCamera1Manager.OpenCamera(mCameraTexture) //为相机设置接收数据的SurfaceTexture

        // 获取 GLSL 中的属性
        uPosHandle = GLES20.glGetAttribLocation(mProgram, "position")
        aTexHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate")
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "textureTransform")
        filterSecondTextureCoordinateAttribute = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate2")
        filterInputTextureUniform = GLES20.glGetUniformLocation(mProgram, "inputImageTexture")
        filterInputTextureUniform2 = GLES20.glGetUniformLocation(mProgram, "inputImageTexture2") // This does assume a name of "inputImageTexture2" for second input texture in the fragment shader

        mPosBuffer = BufferUtil.convertToFloatBuffer(mPosCoordinate)
        mTexBuffer = BufferUtil.convertToFloatBuffer(mTexCoordinate)
        // glVertexAttribPointer()：这个方法用于指定如何在顶点着色器中读取顶点属性数据。
        GLES20.glVertexAttribPointer(
            // index 参数决定了你在顶点着色器中使用哪个顶点属性。这通常对应于在着色器中声明的 layout(location = n)，其中 n 是属性的位置。
            uPosHandle,
            // 这个值表示每个顶点属性的组件数量。在这个例子中，2 表示每个顶点仅有两个分量，通常用于表示二维坐标
            2,
            // 指定每个分量的数据类型为 float。这意味着每个分量的大小为 4 字节（32 位）。
            GLES20.GL_FLOAT,
            // 这个布尔值指定是否需要进行归一化。因为这里是 false，所以不会对顶点属性的值进行归一化处理，直接使用原始值。
            false,
            // 这个参数表示步长（stride），即两个连续顶点属性之间的字节距离。在这里设置为 0，意味着属性数据是连续存储的。
            0,
            // 这是一个缓冲区对象，存储了顶点属性的数据（在这个例子中，是顶点的位置数据）
            mPosBuffer
        )
        GLES20.glVertexAttribPointer(aTexHandle, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer)

        // 这个函数用于启用指定的顶点属性数组，使其在绘制图形时可以被顶点着色器访问。如果不启用这个属性数组，
        // 尽管你已经设置了顶点数据和指针，但在绘制时，着色器将无法读取该属性的数据，导致渲染结果不正确。
        GLES20.glEnableVertexAttribArray(uPosHandle)
        GLES20.glEnableVertexAttribArray(aTexHandle)

        // setRotation()
    }


    private val TEXTURE_NO_ROTATION = floatArrayOf(
        0.0f, 1.0f,
        1.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f
    )
    private fun setRotation() {
        val bBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder())
        val fBuffer = bBuffer.asFloatBuffer()
        fBuffer.put(TEXTURE_NO_ROTATION)
        fBuffer.flip()
        texture2CoordinatesBuffer = bBuffer
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.e(TAG, "onSurfaceChanged() gl = [$gl], width = [$width], height = [$height]")
        GLES20.glViewport(0, 0, width, height)
        val ratio = width.toFloat() / height
        // 创建一个正交投影矩阵 mProjectMatrix ：1和7代表远近视点与眼睛的距离，非坐标点
        Matrix.orthoM(
            // 用于存储生成的正交投影矩阵的数组。
            mProjectMatrix,
            0,
            // 定义X轴的可视范围，从-1到1。
            -1f, 1f,
            // 定义Y轴的可视范围，使用之前计算的宽高比来确保纵横比正确。
            -ratio, ratio,
            // 定义Z轴的可视范围，从1到7，设置可视的深度范围。
            1f, 7f
        )
        // 1代表眼睛的坐标点
        Matrix.setLookAtM(mCameraMatrix, 0, 0f, 0f, 3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        // 将两个4x4的矩阵相乘，并将结果存储在第三个4x4矩阵中。
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectMatrix, 0, mCameraMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10) {
        Log.e(TAG, "onDrawFrame() called with: gl = [$gl]")
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        //通过此方法更新接收到的预览数据
        mCameraTexture!!.updateTexImage()
        // mCameraTexture.getTransformMatrix(mTempMatrix);//获取到图像数据流的坐标变换矩阵
        //  Matrix.multiplyMM(mTempMatrix, 0, mTempMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(
            mMVPMatrixHandle, 1,
            // 指示矩阵是否要转置，这里设置为 false，表示不转置。
            false,
            mMVPMatrix, 0)

        // 绘制几何图形。
        GLES20.glDrawArrays(
            // GLES20.GL_TRIANGLES, // 绘制三角形

            // 绘制方式，这里使用三角形条带： 使用 GL_TRIANGLE_STRIP，每增加一个顶点，就会形成一个新的三角形。
            // 第一个三角形由前三个顶点构成，第二个三角形则利用前两个顶点和最后一个顶点。通过共享顶点，
            // GL_TRIANGLE_STRIP 能够高效地绘制多个三角形。只需设置合适的顶点数组和绘制调用，即可实现这一效果。
            GLES20.GL_TRIANGLE_STRIP,
            0, mPosCoordinate.size / 2
        )

        // drawWatermark()
    }

    // 水印纹理
    private var watermarkTexture = 0
    private fun drawWatermark() {
        // TODO: 2024/10/16 融合操作
        GLES20.glEnableVertexAttribArray(filterSecondTextureCoordinateAttribute)
        GLES20.glActiveTexture(GLES20.GL_TEXTURE3)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, watermarkTexture)
        GLES20.glUniform1i(filterInputTextureUniform2, 3)

        texture2CoordinatesBuffer?.position(0)
        GLES20.glVertexAttribPointer(
            filterSecondTextureCoordinateAttribute,
            2,
            GLES20.GL_FLOAT,
            false,
            0,
            texture2CoordinatesBuffer
        )
    }

    /**
     * 创建显示的texture
     */
    private fun createAndBindVideoTexture() {
        val texture = IntArray(1)
        //生成一个OpenGl纹理
        GLES20.glGenTextures(1, texture, 0)
        //申请纹理存储区域并设置相关参数
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture[0])
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MIN_FILTER,
            // 使用线性过滤，提供平滑的纹理缩小效果
            GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameterf(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR.toFloat()
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            // 设置纹理在水平（S轴）方向的包裹模式。
            GL10.GL_TEXTURE_WRAP_S,
            // 表示纹理坐标超出范围时，使用边缘像素的颜色。
            GL10.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
            // 设置纹理在垂直（T轴）方向的包裹模式。
            GL10.GL_TEXTURE_WRAP_T,
            // 同样处理超出范围的纹理坐标。
            GL10.GL_CLAMP_TO_EDGE
        )
        //以上面OpenGl生成的纹理函数参数创建SurfaceTexture, SurfaceTexture接收的数据将传入该纹理
        mCameraTexture = SurfaceTexture(texture[0])
        //设置SurfaceTexture的回调，通过摄像头预览数据已更新
        mCameraTexture!!.setOnFrameAvailableListener(listener)
        // GLES20.glUniform1i(filterInputTextureUniform, 0)
    }

    fun onDestroy() {
        GLES20.glDisableVertexAttribArray(uPosHandle)
        GLES20.glDisableVertexAttribArray(aTexHandle)
    }

    companion object {
        private const val TAG = "CameraRenderer"
    }
}