package com.android.withCamera

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.android.withCamera.square.MyRenderer
import com.example.opengl.R

class CameraPlayWithOpenglActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_play_with_opengl)

        testSquare()
    }

    private fun testSquare() {
        val view = findViewById<GLSurfaceView>(R.id.square_gl_test)
        view.setEGLContextClientVersion(2)
        view.setRenderer(MyRenderer())
    }
}