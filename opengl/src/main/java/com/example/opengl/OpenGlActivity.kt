package com.example.opengl

import android.app.ActivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

// 参考文章： https://blog.csdn.net/chong_lai/article/details/123610160

class OpenGlActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: AirGLSurfaceView
    private var renderSet = AtomicBoolean(false)
    private val tableVertices: FloatArray = floatArrayOf(
        0f, 0f,
        0f, 14f,
        9f, 14f,
        9f, 0f
    )
    private val tableVerticesWithTriangles: FloatArray = floatArrayOf(
        -0.5F, -0.5F,
        0.5F, 0.5F,
        -0.5F, 0.5F,

        -0.5F, -0.5F,
        0.5F, -0.5F,
        0.5F, 0.5F,
        // 中线
        -0.5F, 0F,
        0.5F, 0F,
        // 顶点
        0F, -0.25F,
        0F, 0.25F,
    )

    private val vertexData: FloatBuffer = ByteBuffer
        .allocateDirect(tableVerticesWithTriangles.size * BYTES_PER_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer()
        .put(tableVerticesWithTriangles)



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = AirGLSurfaceView(this)
        isSupportOenGL()
        setContentView(glSurfaceView)
        // setContentView(R.layout.activity_open_gl)
    }

    private fun isSupportOenGL() {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val configRationInfo = activityManager.deviceConfigurationInfo

        val isSupportEs2 = configRationInfo.reqGlEsVersion >= 0x2000

        if (isSupportEs2) {
            glSurfaceView.setEGLContextClientVersion(2)
            glSurfaceView.setRenderer(AirHockeyRenderer(this, vertexData))
            renderSet.set(true)
        } else {
            throw java.lang.Exception("isSupportEs2 = false")
        }
    }

    override fun onResume() {
        super.onResume()
        if (renderSet.get()) glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        if (renderSet.get()) glSurfaceView.onPause()
    }

    companion object {
        private const val BYTES_PER_FLOAT = 4
        private const val POSITION_COMPONENT_COUNT = 2
    }

}