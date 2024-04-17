package com.example.android_media_lib.block

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.example.base.MyApplication

object BitmapTest {
    private const val TAG = "BitmapTest"
    private const val DEBUG = true

    fun test() {
        testSize()
    }

    private fun testSize() {
        BitmapDrawable()
        val bitmap = BitmapFactory.decodeResource(MyApplication.getContext().resources, com.example.base.R.drawable.ic_ffmpeg)
        Log.d(TAG, "testSize: ${bitmap.byteCount}")
        Log.d(TAG, "testSize config: ${bitmap.config}")
        // bitmap.config = Bitmap.Config.HARDWARE
        bitmap.getPixel(100, 200).also {
            Log.d(TAG, "testSize: pix: $it")
            val alpha: Int = it shr 24 and 0xFF
            val red: Int = it shr 16 and 0xFF
            val green: Int = it shr 8 and 0xFF
            val blue: Int = it and 0xFF
            Log.d(TAG, "testSize: alpha：$alpha $red $green $blue")
        }
        Canvas().also {
            // it.drawBitmap(bitmap, )
        }
        Runtime.getRuntime().maxMemory().also {
            Log.d(TAG, "testSize: runTime:$it")
        }
    }
}

fun main() {
}