package com.example.base.utils

import android.util.Log
import java.io.File

private const val TAG = "YKFileUtils"

fun createFile(dir: String, fileName: String): File? {
    // val directory = File(MyApplication.getContext().filesDir, dir)
    val directory = File(dir)
    Log.d(TAG, "createFile: ${directory.path}")
    if (!directory.exists()) {
        if (!directory.mkdirs()) return null
    }

    val file = File(directory, fileName)
    if (file.exists()) {
        return file
    } else {
        file.createNewFile().also {
            return if (it) file else null
        }
    }
}