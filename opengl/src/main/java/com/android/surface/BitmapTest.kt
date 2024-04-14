package com.android.surface

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import com.android.surface.BitmapTest.test
import com.example.base.MyApplication
import com.example.opengl.R

object BitmapTest {
    private const val TAG = "BitmapTest"
    private const val DEBUG = true

    fun test() {
        testSize()
    }

    private fun testSize() {
        BitmapDrawable()
        val a = BitmapFactory.decodeResource(MyApplication.getContext().resources, R.drawable.air_hockey_surface)
        Log.d(TAG, "testSize: ${a.byteCount}")
    }
}

fun main() {
    test()
}