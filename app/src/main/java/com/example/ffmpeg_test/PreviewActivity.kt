package com.example.ffmpeg_test

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.camera.PreviewManager
import com.example.ffmpeg_test.databinding.ActivityPreviewBinding

// cameraX 使用文档：https://developer.android.google.cn/codelabs/camerax-getting-started?hl=zh-cn#3

class PreviewActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPreviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initEvent()
    }

    private fun initEvent() {
        binding.previewActivityBtnPreview.setOnClickListener {
            PreviewManager.valve(this, binding.previewActivityPreview, this)
        }
    }

    companion object {
        private const val TAG = "PreviewActivity"
    }
}