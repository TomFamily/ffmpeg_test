package com.example.ffmpeg_test

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android_media_lib.MP4Player
import com.example.ffmpeg_test.databinding.ActivityMainBinding
import com.example.ffmpeg_test.jni.FFmpegJni
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
        initTest()
    }

    private fun initTest() {
        MP4Player.test()
    }

    private fun initPermission() {
        PermissionUtil.requestSDCardPermission(this@MainActivity, 1002) { }
    }

    private fun invokeJni() {
        if (!File(video_mp4).exists()) throw RuntimeException(File(video_mp4).absolutePath + " 文件不存在")
        Log.d(TAG, "invokeJni: ${fFmpegJni.initConfig(video_mp4)}")

        // fFmpegJni.playVideoWithFilter()
    }

    private fun initEvent() {
        binding.mainBtnTestPlay.setOnClickListener {
            if (!File(video_mp4).exists()) throw RuntimeException(File(video_mp4).absolutePath + " 文件不存在")
            thread?.interrupt()
            thread = Thread {
                // 一定放在子现场操作，否则奔溃
//                fFmpegJni.playVideoWithFilter(
//                    video_mp4,
//                    binding.mainSvTest.holder.surface,
//                    FilterEffect.DRAW_RECTS.value
//                )
                fFmpegJni.mixVideoAndMusic(
                    audio_mp3,
                    video_flv,
                    output_flv
                ).apply {
                    Log.d(TAG, "initEvent mixVideoAndMusic: $this")
                }
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
        private val video_mp4 = "/storage/emulated/0/input.mp4"
        private val video_flv = "/storage/emulated/0/yy_video.flv"
        private val audio_mp3 = "/storage/emulated/0/yy_audio.mp3"
        private val output_flv = "/storage/emulated/0/ffmpeg_out/output_flv.flv"

    }
}