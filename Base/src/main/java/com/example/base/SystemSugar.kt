package com.example.base

import android.util.Log
import java.util.concurrent.Callable
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

object SystemSugar {
    private const val TAG = "SystemSugar"
    fun test() {

    }

    private fun testInterface() {
        val runnable = Runnable {
            Log.d(TAG, "testInterface: Runnable")
        }

        val callable = object : Callable<Int> {
            override fun call(): Int {
                return 3
            }
        }

        val future = object : Future<Int> {
            override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
                TODO("Not yet implemented")
            }

            override fun isCancelled(): Boolean {
                TODO("Not yet implemented")
            }

            override fun isDone(): Boolean {
                TODO("Not yet implemented")
            }

            override fun get(): Int {
                TODO("Not yet implemented")
            }

            override fun get(timeout: Long, unit: TimeUnit?): Int {
                TODO("Not yet implemented")
            }

        }
    }
}