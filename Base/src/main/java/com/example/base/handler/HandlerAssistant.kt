package com.example.base.handler

import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log

/**
 * HandlerThread 是 Android 提供的一个带有消息循环的线程类，通常用于在后台执行长时间运行的任务，
 * 并通过 Handler 与主线程进行通信。它是 Thread 的子类，同时实现了 Looper，因此可以方便地与 Handler
 * 结合使用来处理消息和任务。
 *
 * 1、消息处理：HandlerThread 继承自 Thread 并且初始化了 Looper，这使得它可以像主线程那样处理消息。你可以创建
 * 一个 Handler 对象并将其与 HandlerThread 关联，然后利用该 Handler 向 HandlerThread 发送消息，
 * 从而执行异步任务。
 *
 * 2、后台任务：适合用于需要在后台长时间执行的任务，比如下载文件、处理数据、执行网络请求等。使用 HandlerThread
 * 可以避免在主线程中执行这些任务导致界面卡顿或 ANR（Application Not Responding）问题。
 *
 * 3、与主线程通信：通过在主线程创建一个 Handler，可以将消息发送到 HandlerThread，并在
 * HandlerThread 中执行对应的处理逻辑。这样可以实现主线程与后台线程之间的数据传递和通信。
 */
object HandlerAssistant {
    private const val TAG = "HandlerAssistant"

    private val handlerThread by lazy { HandlerThread(TAG + "Thread") }
    private var handler: Handler

    init {
        handlerThread.start()
        handler = object : Handler(handlerThread.looper){
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                //执行的UI操作
                when(msg.what) {
                    1 -> {
                        Log.d(TAG, "handleMessage: ${Thread.currentThread()}")
                        post {
                            Log.d(TAG, "handleMessage3: ${Thread.currentThread()}")
                        }
                    }
                    2 -> {
                        Log.d(TAG, "handleMessage2: ${Thread.currentThread()}")
                    }
                }
            }
        }
    }

    fun test(what: Int) {
        Log.d(TAG, "test: ")
        handler.obtainMessage().also {
            it.what = what
            handler.sendMessage(it)
        }
    }

    fun quit() {
        handlerThread.quit()
        // handlerThread.quitSafely()
    }
}



class ExampleHandlerThread : HandlerThread("ExampleHandlerThread") {
    private var handler: Handler? = null

    override fun onLooperPrepared() {
        super.onLooperPrepared()
        handler = object : Handler(looper) {
            override fun handleMessage(msg: Message) {
                // 在 HandlerThread 中处理消息
                // 可以执行耗时操作，比如网络请求、数据库操作等
                // 完成后向主线程发送消息或更新UI
            }
        }
    }

    fun executeTaskInBackground() {
        handler!!.post { // 执行后台任务
            // 这里可以进行网络请求、数据库操作等耗时操作
            // 完成后可以通过 handler 向主线程发送消息
            handler!!.obtainMessage(1, "Task completed").sendToTarget()
        }
    }
}