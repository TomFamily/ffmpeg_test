package com.example.io_model.b_io

import android.util.Log
import com.example.base.MyApplication
import java.io.*

private const val TAG = "FileOperate"
fun fileIO(fileName: String = "test.txt") {
    val file = File(MyApplication.getContext().filesDir, fileName)
    Log.d(TAG, "fileIO: ${file.path}")

    val writer = BufferedWriter(FileWriter(file))
    val reader = BufferedReader(FileReader(file))
    try {

        // 写入文件
        writer.write("Hello, World!")
        writer.newLine()
        writer.write("Goodbye, World!")
        writer.flush()

        // 读取文件
        var line: String? = reader.readLine()
        while (line != null) {
            Log.d(TAG, "fileIO: $line")
            line = reader.readLine()
        }
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        reader.close()
        writer.close()
    }
}