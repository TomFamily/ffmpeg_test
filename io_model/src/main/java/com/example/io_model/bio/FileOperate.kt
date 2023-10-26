package com.example.io_model.bio

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.base.MyApplication
import java.io.*

private const val TAG = "FileOperate"

/**
 * 获取外部存储路径操作权限（包名下的文件属于内部目录，不需要申请权限就可以操作）
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 */
fun requestPermission(context: Context) {
    if (ContextCompat.checkSelfPermission(
            context, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        // 未获得权限，需要申请
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100
        )
    } else {
        // 已获得权限，执行相关操作
        Log.d(TAG, "permission: 已经有权限")
    }
}

fun fileIO(fileName: String = "test.txt") {
    val file = File(MyApplication.getContext().filesDir, fileName)
    // val file = File("/sdcard/test", fileName)
    Log.d(TAG, "fileIO: ${file.path}")

    // 如果文件不存在，会创建文件
    val writer = BufferedWriter(FileWriter(file))
    val reader = BufferedReader(FileReader(file))
    Thread {
        try {
            // 写入文件
            writer.write("Hello, World!")
            writer.newLine()
            writer.write("Goodbye, World!")
            writer.flush()

            // 读取文件
            var line: String? = reader.readLine()
            while (line != null) {
                Log.d(TAG, "fileIO: $line ${Thread.currentThread()}")
                line = reader.readLine()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            // TODO: 一定要关闭，否则文件内容可能丢失
            reader.close()
            writer.close()
        }
    }.start()
}