package com.example.ffmpeg_test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ffmpeg_test.databinding.ActivityMainBinding
import com.example.ffmpeg_test.jni.FFmpegJni
import com.example.ffmpeg_test.jni.FilterEffect
import com.example.ffmpeg_test.uitls.PermissionUtil
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val fFmpegJni by lazy { FFmpegJni() }
    private var thread: Thread? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPermission()
        initEvent()
        invokeJni()
    }
    private fun initPermission() {
        PermissionUtil.requestSDCardPermission(this@MainActivity, 1002) { }
    }

    private fun invokeJni() {
        if (!File(path).exists()) throw RuntimeException(File(path).absolutePath + " 文件不存在")
        Log.d(TAG, "invokeJni: ${fFmpegJni.initConfig(path)}")

        // fFmpegJni.playVideoWithFilter()
    }

    private fun initEvent() {
        binding.mainBtnTestPlay.setOnClickListener {
            if (!File(path).exists()) throw RuntimeException(File(path).absolutePath + " 文件不存在")
            thread?.interrupt()
            thread = Thread {
                // 一定放在子现场操作，否则奔溃
                Log.d(TAG, "invokeJni: " +
                        "${fFmpegJni.playVideoWithFilter(path, binding.mainSvTest.holder.surface, FilterEffect.DRAW_RECTS.value)}")
            }
            thread?.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        thread?.interrupt()
    }

    companion object {
        private const val TAG = "MainActivity11"
        @SuppressLint("SdCardPath")
        private val path = "/storage/emulated/0/input.mp4"
    }
}