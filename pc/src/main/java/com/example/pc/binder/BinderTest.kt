package com.example.pc.binder

import android.content.Context
import android.util.Log
import com.map.locator.data.MapLocatorDataManager

private const val TAG = "BinderTest"

fun testBinder(context: Context) {
    binderBinding(context)
}

private fun binderBinding(context: Context) {
    Log.d(TAG, "binderBinding:")
    MapLocatorDataManager.getInstance().init(context)
    MapLocatorDataManager.getInstance().setOnMapLocatorDataListener{

    }
}