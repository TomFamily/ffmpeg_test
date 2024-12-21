package com.android.withCamera

import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.opengl.R

class CameraPlayWithOpenglActivity : AppCompatActivity() {
    private lateinit var graphicalSurfaceView: SurfaceView
    private lateinit var bitmapSurfaceView: SurfaceView
    private lateinit var orthogonalSurfaceView: SurfaceView
    private lateinit var texturesSurfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_play_with_opengl)

        initView()
        initEvent()
    }

    private fun initView() {
        graphicalSurfaceView = findViewById(R.id.test_graphical)
        bitmapSurfaceView = findViewById(R.id.test_bitmap)
        orthogonalSurfaceView = findViewById(R.id.test_orthogonal)
        texturesSurfaceView = findViewById(R.id.test_textures)
    }

    private fun initEvent() {
        findViewById<Button>(R.id.test_graphical_btn).setOnClickListener {
            graphicalSurfaceView.also {
                it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        findViewById<Button>(R.id.test_bitmap_btn).setOnClickListener {
            bitmapSurfaceView.also {
                it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        findViewById<Button>(R.id.test_orthogonal_btn).setOnClickListener {
            orthogonalSurfaceView.also {
                it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        findViewById<Button>(R.id.test_textures_btn).setOnClickListener {
            texturesSurfaceView.also {
                it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }
    }

}