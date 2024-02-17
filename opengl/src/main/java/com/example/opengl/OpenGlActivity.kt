package com.example.opengl

import android.app.ActivityManager
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.util.concurrent.atomic.AtomicBoolean

class OpenGlActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private var renderSet = AtomicBoolean(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this)
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
            glSurfaceView.setRenderer(FirstOpenglRender())
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
}