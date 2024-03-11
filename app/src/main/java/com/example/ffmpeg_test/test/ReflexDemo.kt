package com.example.ffmpeg_test.test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

fun testReflex() {
    testDynamicProxy()
    testStaticProxy()
}

// 定义接口
private interface Subject {
    fun doAction()
}

// 实际的主题类
private class RealSubject : Subject {
    override fun doAction() {
        println("RealSubject: Performing action...")
    }
}

// InvocationHandler 实现类，用于拦截方法调用并执行额外逻辑
private class ProxyHandler(private val target: Any) : InvocationHandler {
    override fun invoke(proxy: Any, method: Method, args: Array<Any>?): Any? {
        // 在方法调用前执行额外逻辑
        println("Before invoking ${method.name}")

        // 调用实际对象的方法
        val result = method.invoke(target, *(args ?: emptyArray()))

        // 在方法调用后执行额外逻辑
        println("After invoking ${method.name}")

        return result
    }
}

/**
 * 动态代理
 */
private fun testDynamicProxy() {
    // 创建实际的主题对象
    val realSubject = RealSubject()

    // 创建动态代理对象
    val proxySubject = Proxy.newProxyInstance(
        Subject::class.java.classLoader, arrayOf(Subject::class.java), ProxyHandler(realSubject)
    ) as Subject

    // 通过代理对象调用方法
    proxySubject.doAction()
}

// 定义接口
private interface Printer {
    fun printMessage()
}

// 实际的打印机类
private class RealPrinter : Printer {
    override fun printMessage() {
        println("Printing from Real Printer")
    }
}

// 代理类，实现了 Printer 接口，并委托给 RealPrinter
private class PrinterProxy(private val realPrinter: Printer) : Printer {
    override fun printMessage() {
        // 可以在这里添加一些额外的逻辑
        println("Before printing...")
        realPrinter.printMessage() // 委托给真正的打印机
        println("After printing...")
    }
}

/**
 * 静态代理
 */
private fun testStaticProxy() {
    // 创建真正的打印机实例
    val realPrinter = RealPrinter()
    // 创建代理类实例，传入真正的打印机实例
    val printerProxy = PrinterProxy(realPrinter)
    // 使用代理类来打印消息
    printerProxy.printMessage()
}

