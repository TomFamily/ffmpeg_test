package com.example.ffmpeg_test

import android.annotation.SuppressLint
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.SurfaceHolder
import androidx.appcompat.app.AppCompatActivity
import com.example.ffmpeg_test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(),  SurfaceHolder.Callback{

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.mainTvTest.text = ffmpegInfo()
        Log.d(TAG, "onCreate: ${getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}")

        binding.mainSvTest.holder.addCallback(this)
        binding.mainTvTest.setOnClickListener {
            Log.d(TAG, "onCreate: setOnClickListener")
            //子线程进行视频渲染
        }
        binding.mainSvTest.holder.setFormat(PixelFormat.RGBA_8888)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated: ")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    companion object {
        private const val TAG = "MainActivity11"
        @SuppressLint("SdCardPath")
        private val path = "/sdcard/DCIM/Camera/VID_20231008_094739.mp4"
        init {
            System.loadLibrary("native-lib")
        }
    }
}