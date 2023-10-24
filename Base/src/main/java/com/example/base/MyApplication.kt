package com.example.base

import android.app.Application
import android.content.Context
import android.util.Log

class MyApplication: Application() {
    companion object {
        private const val TAG = "MyApplication"
        private lateinit var instance: MyApplication

        fun getContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        instance = this
    }
}