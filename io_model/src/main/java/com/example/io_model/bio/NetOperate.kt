package com.example.io_model.bio

import android.util.Log
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset

private const val TAG = "NetOperate"

fun startSocketServer(port: Int = 8080) {
    // 创建ServerSocket并监听指定端口
    val serverSocket = ServerSocket(port)
    Log.d(TAG, "startSocketServer: 服务器启动，监听端口: $port")

    while (true) {
        // 等待客户端连接
        val clientSocket = serverSocket.accept()
        Log.d(TAG, "startSocketServer: 收到客户端连接：${clientSocket.inetAddress.hostAddress}")

        // 获取输入输出流
        val inputStream = clientSocket.getInputStream()
        val outputStream = clientSocket.getOutputStream()

        // 处理客户端请求
        val requestData = inputStream.reader(Charset.defaultCharset())
        // val requestData = inputStream.reader(Charset.defaultCharset()).readLine()
        Log.d(TAG, "startSocketServer: 收到客户端请求：$requestData")

        // 发送响应数据给客户端
        val responseData = "Hello from server"
        outputStream.write(responseData.toByteArray())

        // 关闭连接
        clientSocket.close()
    }
}

fun startSocketClient(hostname: String = "localhost", port: Int = 8080) {
    // 创建Socket连接服务器
    val socket = Socket(hostname, port)
    Log.d(TAG, "startSocketClient: 连接到服务器：$hostname:$port")

    // 获取输入输出流
    val inputStream = socket.getInputStream()
    val outputStream = socket.getOutputStream()

    // 发送请求数据给服务器
    val requestData = "Hello from client"
    outputStream.write(requestData.toByteArray())

    // 接收服务器响应
    // val responseData = inputStream.reader(Charset.defaultCharset()).readLine()
    val responseData = inputStream.reader(Charset.defaultCharset())
    Log.d(TAG, "startSocketClient: 收到服务器响应：$responseData")

    // 关闭连接
    socket.close()
}