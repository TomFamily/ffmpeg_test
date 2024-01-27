package com.example.ffmpeg_test

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.android_media_lib.MP4Player
import com.example.android_media_lib.MyAudioRecord
import com.example.aop_aspect.DebugLog
import com.example.base.AUDIO_FILE_NAME
import com.example.base.BASE_PATH_MEDIA
import com.example.base.async.ThreadHelper
import com.example.base.config.*
import com.example.base.rxjava.*
import com.example.ffmpeg_test.databinding.ActivityMainBinding
import com.example.ffmpeg_test.jni.FFmpegJni
import com.example.ffmpeg_test.uitls.FunctionTestInterface
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
        testMediaPlayer()
        FunctionTestInterface.testIOModel()
        test()
    }

    private fun test() {
        testRouter()
        testRxjava(this)

        val threadHelper = ThreadHelper()
        binding.mainBtnTest.setOnClickListener {
            threadHelper.startRunnable {
                Log.d(TAG, "test: ${Thread.currentThread().name} ${Thread.currentThread().priority}")
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun testMediaPlayer() {
        // MP4Player().start(binding.mainSvTest.holder, path = output_flv)

        val myAudioRecord = MyAudioRecord()
        binding.mainMediaInclude.mainAudioRecord.setOnClickListener {
            myAudioRecord.startRecording(BASE_PATH_MEDIA, AUDIO_FILE_NAME)
        }

        binding.mainMediaInclude.mainAudioRecordStop.setOnClickListener {
            myAudioRecord.stopRecording()
        }
    }

    @DebugLog
    private fun initPermission() {
        PermissionUtil.requestSDCardPermission(this@MainActivity, 1002) { }
        PermissionUtil.requestPermission(this, Manifest.permission.RECORD_AUDIO, 0)
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
        Log.d(TAG, "onDestroy: ")
        thread?.interrupt()
    }

    companion object {
        private const val TAG = "MainActivity11"
    }
}