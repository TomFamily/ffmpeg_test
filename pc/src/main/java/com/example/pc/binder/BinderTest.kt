package com.example.pc.binder

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import com.example.pc.service.TestService
import com.map.locator.data.MapLocatorDataManager

private const val TAG = "BinderTest"
private lateinit var mTestService: TestService

fun testBinder(context: Context) {
    binderBinding(context)
    bindServices(context)
}

private fun performClick() {
    mTestService.testData()
//    Timer().schedule(object : TimerTask() {
//        override fun run() {
//            mTestService.testData()
//        }
//    }, 0, 2000)
}

private fun bindServices(context: Context) {
    val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            (service as TestService.LocalBinder).apply {
                mTestService = this.service
                performClick()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: ")
        }
    }

    Intent(context, TestService::class.java).also {
        // 启动方式一：
        // context.startService(it)

        // 启动方式二：
        context.bindService(it, serviceConnection, Context.BIND_AUTO_CREATE)
    }
}

private fun binderBinding(context: Context) {
    Log.d(TAG, "binderBinding:")
    MapLocatorDataManager.getInstance().init(context)
    MapLocatorDataManager.getInstance().setOnMapLocatorDataListener {}
}