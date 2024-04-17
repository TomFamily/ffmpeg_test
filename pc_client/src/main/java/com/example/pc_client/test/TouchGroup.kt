package com.example.pc_client.test

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.pc_client.databinding.ViewMyTestBinding

class TouchGroup(context: Context, attributeSet: AttributeSet): ConstraintLayout(context, attributeSet) {
    private val binding: ViewMyTestBinding
    init {
        binding = ViewMyTestBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        initEvent()
    }

    private fun initEvent() {
        // 执行 1：先于 onTouchEvent 执行
        binding.touchGroupIv.setOnTouchListener { v, event ->
            Log.d(TAG, "initEvent setOnTouchListener: ${event.action}")
            return@setOnTouchListener false
        }

        //  执行 4
        binding.touchGroupIv.setOnClickListener {
            Log.d(TAG, "initEvent: setOnClickListener")
        }

        binding.touchGroupIv.setOnLongClickListener {
            Log.d(TAG, "initEvent: long")
            return@setOnLongClickListener true
        }
    }

    //  执行 2
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        Log.d(TAG, "onTouchEvent: ${event?.action}")
        return super.onTouchEvent(event)
    }

    companion object {
        private const val TAG = "MyTestView"
    }
}