package com.example.ffmpeg_test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.ffmpeg_test.databinding.ActivityMainBinding
import com.example.ffmpeg_test.jni.FFmpegJni
import com.example.ffmpeg_test.uitls.PermissionUtil
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initPermission()
        initView()
        invokeJni()
    }
    private fun initPermission() {
        PermissionUtil.requestSDCardPermission(this@MainActivity, 1002) { }
    }

    private fun invokeJni() {
        FFmpegJni().apply {
            val input = File(path)
            if (!input.exists()) throw RuntimeException(input.absolutePath + " 文件不存在")
            Log.d(TAG, "invokeJni: ${initConfig(path)}")
        }
    }

    private fun initView() {
    }


    companion object {
        private const val TAG = "MainActivity11"
        @SuppressLint("SdCardPath")
        private val path = "/storage/emulated/0/input.mp4"
    }
}