package com.example.io_model.nio

import android.annotation.SuppressLint
import android.util.Log
import com.example.base.MyApplication
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption


object NioFileOperate {
    private const val TAG = "NioFileOperate"

    @SuppressLint("CheckResult")
    fun fileOperate(fileName: String = "test.txt") {
        val filePath = File(MyApplication.getContext().filesDir, fileName)
        val file = Paths.get(filePath.path)
        val buffer = ByteBuffer.allocate(1024)
        // 异步读取文件
        Observable.create { emitter ->
            try {
                FileChannel.open(file, StandardOpenOption.READ).use { channel ->
                    while (channel.read(buffer) != -1) {
                        buffer.flip()
                        val byteArray = ByteArray(buffer.remaining())
                        buffer.get(byteArray) // 从 ByteBuffer 中读取数据到字节数组
                        val text = String(byteArray, Charset.forName("UTF-8")) // 转换为字符串
                        emitter.onNext(text) // 发送文本到监听者
                        buffer.clear()
                    }
                }
            } catch (e: IOException) {
                emitter.onError(e) // 发生错误时发送错误通知给监听者
            }
            emitter.onComplete() // 操作完成时发送完成通知给监听者
        }.subscribe({
            Log.d(TAG, "fileOperate create: $it")
        }, {
            Log.d(TAG, "fileOperate create2: $it")
        }, {
            Log.d(TAG, "fileOperate: end")
        })

        // 异步写入文件
        val data = "Hello, World!".toByteArray()
        Observable.fromCallable {
            try {
                FileChannel.open(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE)
                    .use { channel ->
                        channel.write(ByteBuffer.wrap(data))
                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            0 // 返回0或其他适当的值来表示操作完成
        }.subscribe {
            Log.d(TAG, "fileOperate2: $it")
        }
    }

    @SuppressLint("CheckResult")
    fun fileOperateRxCallable(fileName: String = "test.txt") {
        val filePath = File(MyApplication.getContext().filesDir, fileName).path

        Observable.fromCallable {
            String(Files.readAllBytes(Paths.get(filePath)))
        }.subscribeOn(Schedulers.io()).observeOn(Schedulers.computation()).subscribe({ content ->
                Log.d(TAG, "fileOperateRxCallable: $content")
            }, { error ->
                Log.d(TAG, "fileOperateRxCallable Error occurred: ${error.message}")
            })


        val file = Paths.get(filePath)
        val buffer = ByteBuffer.allocate(1024)
        // 异步读取文件
        val readObservable = Observable.fromCallable {
            try {
                FileChannel.open(file, StandardOpenOption.READ).use { channel ->
                    while (channel.read(buffer) != -1) {
                        buffer.flip()
                        val byteArray = ByteArray(buffer.remaining())
                        buffer.get(byteArray) // 从 ByteBuffer 中读取数据到字节数组
                        val text = String(byteArray, Charset.forName("UTF-8")) // 转换为字符串
                        Log.d(TAG, "fileOperate readObservable: $text ${Thread.currentThread()}") // 打印字符串
                        buffer.clear()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            0 // 返回0或其他适当的值来表示操作完成
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                Log.d(TAG, "fileOperate2: $it ${Thread.currentThread()}")
            }
    }
}

