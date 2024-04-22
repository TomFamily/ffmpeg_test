package com.example.pc_client

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication: Application() {

    companion object {
        private const val TAG = "MyApplication"
    }
}