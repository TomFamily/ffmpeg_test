package com.example.base.async

import android.util.Log

class ThreadHelper: Thread() {
    private var runnable: Runnable? = null

    init {
        this.priority = 4
    }

    fun startRunnable(runnable: Runnable) {
        this.runnable = runnable
        Log.d(TAG, "startRunnable: ${currentThread().name} ${currentThread().priority}")
        start()
    }

    override fun run() {
        super.run()
        runnable?.run()
    }

    companion object {
        private const val TAG = "ThreadHelper"
    }
}