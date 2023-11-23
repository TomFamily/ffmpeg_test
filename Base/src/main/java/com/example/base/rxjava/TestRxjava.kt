package com.example.base.rxjava

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 *Created by arno.yang
 *Created on 2023/10/25 10:47
 *PackageName com.example.jni_test.test
 */

private const val TAG = "TestRxjava"

fun dealRealTimeEvent() {
    /**
     * 通过window创建一个一秒时长的窗口，取窗口中的3个数据（为了确保至少有三个数据），当满足条件数据，往下执行
     *
     * window 操作符创建了一个包含多个小 Observable 的 Observable，每个小 Observable 代表一个时间窗口内的事件。
     * flatMap 用于将每个小 Observable 展开为一个事件流，然后在 subscribe 中，处理每个窗口中的点击事件。
     * 在 RxJava 中，window 操作符会创建一个包含多个窗口的 Observable，每个窗口是一个 Observable。
     * 每当窗口关闭时，window 会发射一个通知对象 Notification，而不是一个实际的 List 或其他集合类型。通知对象包含了窗口中的所有事件，
     * 但是它不是一个 List 类型，因此尝试直接将其转换为 List 会导致 ClassCastException。
     *
     * 缺点：会启动一个定时器
     *
     */
    Observable.range(10,20)
        .observeOn(AndroidSchedulers.mainThread())
        .window(1, TimeUnit.SECONDS) // 划分一秒的时间窗口
        .flatMap { window: Observable<Int> -> window.take(3).toList().toObservable() } // 从每个窗口中取前五个事件
        .doOnNext { Log.d(TAG, "testKJ: ${it.size}") }
        .filter { it.size >= 3 }
        .subscribe {
            Log.d(TAG, "testKJ222: $it")
        }.dispose()

    /**
     * 每5个数据归位一组，一组数据的第一个与最后一个的时间间隔在1秒内，就往下执行
     */
    Observable.range(10,20)
        .map { System.currentTimeMillis() }
        .buffer(5)
        .doOnNext { Log.d(TAG, "testKJ22: $it") }
        .filter { o: List<Long> -> o[o.size - 1] - o[0] < 1000 }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe {
            Log.d(TAG, "testKJ: ssss")
        }.dispose()
}

fun testThread() {
    Single.just(0)
        .cache()
        .concatWith {  }
        .subscribe {
            Log.d(TAG, "testThread: $it") 
        }.dispose()

    /**
     * 1、subscribeOn 必须写在 observeOn 之前才会有效
     * 2、多个 subscribeOn， 只有第一个 subscribeOn 会生效
     */
    Observable.create {
        it.onNext(false)
    }
        .subscribeOn(Schedulers.io())
        .doOnNext { Log.d(TAG, "testThread2: ${Thread.currentThread()}") }
        .subscribeOn(Schedulers.newThread())
        .doOnNext { Log.d(TAG, "testThread22: ${Thread.currentThread()}") }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { Log.d(TAG, "testThread1: ${Thread.currentThread()}") }
        .subscribeOn(Schedulers.io())
        .doOnNext { Log.d(TAG, "testThread3: ${Thread.currentThread()}") }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext { Log.d(TAG, "testThread4: ${Thread.currentThread()}") }
        .subscribe()


    Log.d(TAG, "testThread: 222 ${System.currentTimeMillis()}")
    /**
     * empty() ： 直接发送 onComplete() 事件
     * Observable.empty 不会发送值，所以：doOnNext、subscribe 操作符都是无法监听到数据的，
     */
    Observable.empty<Any>()
        .delay(1000, TimeUnit.MILLISECONDS)
        .subscribeOn(Schedulers.io())
        .doOnComplete {
            Log.d(TAG, "testThread: 222 ${System.currentTimeMillis()} ${Thread.currentThread()}")
        }
        .doOnNext { Log.d(TAG, "testThread: 222") }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnComplete {
            Log.d(TAG, "testThread: 222 ${System.currentTimeMillis()} ${Thread.currentThread()}")
        }
        .subscribe {
            Log.d(TAG, "testThread: 222")
        }.dispose()

    Observable.create {
        it.onNext(true)
    }
        .subscribeOn(Schedulers.io())
        .doOnNext { Log.d(TAG, "testThread11: ${Thread.currentThread()}") }
        .flatMap { state ->
            val obj = Observable.create { it.onNext(0) }
            if (state) {
                Log.d(TAG, "testThread: state ${System.currentTimeMillis()}")
                obj.subscribeOn(Schedulers.io())
                    .delay(1000, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        Log.d(TAG, "testThread: state2 ${System.currentTimeMillis()}")
                    }.subscribe()
            } else {
                obj.observeOn(AndroidSchedulers.mainThread())
                    .doOnNext {
                        Log.d(TAG, "testThread: state3 ${System.currentTimeMillis()}")
                    }.subscribe()
            }
            return@flatMap obj
        }
        .observeOn(AndroidSchedulers.mainThread())
        .doOnNext {
            Log.d(TAG, "testThread: state4 ${System.currentTimeMillis()}")
        }
        .subscribe().dispose()


    /**
     * 在RxJava中，compose操作符用于将一系列的操作符封装为一个可重用的操作符。它可以帮助简化代码，并提高代码的可读性和可维护性。
     * 使用compose操作符时，可以通过：ObservableTransformer 创建一个自定义的操作符，该操作符由多个其他操作符组成。
     */
    val customOperator: ObservableTransformer<Int?, String?>? =
        ObservableTransformer { upstream: Observable<Int> ->
            upstream
                .filter { number: Int -> number % 2 == 0 } // 过滤偶数
                .map { number: Int -> "Even: $number" } // 转换为字符串
                .observeOn(AndroidSchedulers.mainThread())
        }

    /**
     * distinctUntilChanged false 可过
     * filter true 可过
     */
    Observable.just(1, 2, 3, 4, 5)
        .distinctUntilChanged { t1, t2 ->
            true
        }
        .compose(customOperator)
        .subscribe {

        }.dispose()


    /**
     * defer：直到被观察者被订阅后才会创建被观察者
     *
     * cache：使用cache操作符对这个Observable进行缓存处理。接下来，我们创建两个订阅者，它们都会订阅同一个Observable。
     * 当第一个订阅者订阅时，Creating Observable会被打印，然后会收到并打印三个字符串数据。当第二个订阅者订阅时，
     * 不会再次触发Creating Observable，而是直接将之前缓存的数据发送给第二个订阅者，从而实现数据的共享。
     * 通过使用cache操作符，您可以确保多个订阅者共享同一份数据，并避免重复执行Observable的创建和数据发射过程。
     * 这在需要缓存结果或共享数据的场景中非常有用。
     */
    val observable = Observable.defer {
        Log.d("Example", "Creating Observable")
        // 这里可以放一些需要延迟执行的操作
        Observable.just("Data 1", "Data 2", "Data 3")
    }.cache()

    observable.subscribe {
        Log.d("Example", "Subscriber 1 received: $it")
    }.dispose()

    observable.subscribe {
        Log.d("Example", "Subscriber 2 received: $it")
    }.dispose()

    /**
     * concatWith操作符用于将当前Observable的发射物品与另一个Observable的发射物品连接起来，形成一个新的Observable。它会按照顺序先发射当前Observable的所有数据，然后再发射另一个Observable的数据。
     */

}