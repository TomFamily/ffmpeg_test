package com.example.camera

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class GLCameraActivity : AppCompatActivity() {

    private lateinit var mainGlSurface: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_glcamera)

        initView()
        initEvent()
    }

    private fun initView() {
        mainGlSurface = findViewById(R.id.mainGlSurface)
    }

    private fun initEvent() {
        findViewById<View>(R.id.glCamera_test_GLSurface).setOnClickListener {
            CameraOpenGLManager(this, mainGlSurface).also {
                it.openCamera()
            }
        }
    }
}