package com.android.opengl

import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.android.opengl.codec.CodecManager
import com.android.opengl.offscreen.FboSurfaceView
import com.example.base.floating.OpenGLFloatingImage
import com.example.opengl.R

class CameraPlayWithOpenglActivity : AppCompatActivity() {
    private lateinit var graphicalSurfaceView: SurfaceView
    private lateinit var bitmapSurfaceView: SurfaceView
    private lateinit var orthogonalSurfaceView: SurfaceView
    private lateinit var texturesSurfaceView: SurfaceView
    private lateinit var camera1OpenglSurfaceView: SurfaceView

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
        camera1OpenglSurfaceView = findViewById(R.id.test_camera1)

        OpenGLFloatingImage.setImageView(findViewById(R.id.test_image_floating))
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

        findViewById<Button>(R.id.test_camera1_btn).setOnClickListener {
            camera1OpenglSurfaceView.also {
                it.visibility = if (it.visibility == View.GONE) View.VISIBLE else View.GONE
            }
        }

        findViewById<Button>(R.id.test_fbo_btn).setOnClickListener {
            testFBO()
        }
        
        initCodecSurface()
    }

    private lateinit var mFboSurfaceView: FboSurfaceView
    private fun testFBO() {
        mFboSurfaceView = FboSurfaceView(this)
    }


    private fun initCodecSurface() {
        val view = findViewById<SurfaceView>(R.id.test_codec_surface)
        view.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                CodecManager.initDecode(holder.surface)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder, format: Int, width: Int, height: Int
            ) = Unit

            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

        })
    }

}