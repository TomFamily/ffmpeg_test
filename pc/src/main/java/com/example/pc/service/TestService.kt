package com.example.pc.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log

class TestService : Service() {
    
    private val binder: IBinder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: TestService
            get() = this@TestService // 返回当前服务的实例
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: ")
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind: ")
        // 返回IBinder对象，供Activity绑定服务使用
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d(TAG, "onUnbind: ")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    //定义测试方法
    fun testData() {
        Log.d(TAG, "testData")
    }

    companion object {
        private const val TAG = "TestService"
    }
}