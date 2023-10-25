package com.example.ffmpeg_test.uitls

import com.example.io_model.bio.fileIO
import com.example.io_model.nio.NioFileOperate.fileOperate
import com.example.io_model.nio.NioFileOperate.fileOperateRxCallable

object FunctionTestInterface {
    private const val TAG = "FunctionTestInterface"

    fun testIOModel() {
        fileIO()
        fileOperate()
        fileOperateRxCallable()
    }
}