package com.example.base.rxjava

import android.util.Log
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
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

fun testWindow() {
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

}

fun testBuffer() {
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

fun testConcatWith() {
    /**
     * concatWith 操作符用于将当前Observable的发射物品与另一个Observable的发射物品连接起来，形成一个新的Observable。
     * 它会按照顺序先发射当前Observable的所有数据，然后再发射另一个Observable的数据。
     */
    Single.just(0)
        .cache()
        .concatWith {  }
        .subscribe {
            Log.d(TAG, "testThread: $it")
        }.dispose()
}

/**
 * 1、subscribeOn 必须写在 observeOn 之前才会有效
 * 2、多个 subscribeOn， 只有第一个 subscribeOn 会生效
 */
fun testThread() {
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
}

fun testEmpty() {
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
}

fun testFlatMap() {
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
        }.subscribe().dispose()
}

fun testCompose() {
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
}

fun testDefer() {
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
}

fun testAndThen() {
    /**
     * andThen 可以组合两个 Completable，在前一个 Completable 执行完成后，才会再执行第二个 Completable
     * 在本例中，将 completable1、completable2 组合，在 completable1 执行完后，completable2才会执行
     */
    val completable1 = Completable.fromRunnable { Log.d(TAG, "testAndThen: completable1") }
    val completable2 = Completable.fromRunnable { Log.d(TAG, "testAndThen: completable2") }

    completable1.andThen(completable2)
        .subscribe {
            Log.d(TAG, "testAndThen: All completable done")
        }.dispose()
}

fun testZip() {
    /**
     * zip用于将多个 Observable 发射的数据按照一个函数组合成一个新的数据项并发射出去。这个操作符能够将多个
     * Observables 的数据项进行配对，从而创建一个新的数据项集合。
     *
     * 注意：
     *   1、zip 操作符会等待所有 Observable 都至少发射了一个数据项后再开始组合。如果某个 Observable 没有发射数据，
     *      zip 将不会发射任何数据项，直到所有 Observables 都发射了至少一项数据。
     *   2、如果其中一个 Observable 发射的数据项比另一个 Observable 发射的数据项更少，那么 zip 操作符只会发射与
     *      最少项数量相同的数据项。多出来的数据项将被忽略。
     */
    val numbers = Observable.just(1, 2, 3, 4, 5)
    val words = Observable.just("One", "Two", "Three", "Four", "Five")

    Observable.zip(numbers, words) { number, word ->
        "$number - $word"
    }.subscribe { result ->
        Log.d(TAG, "testZip: $result")
        }.dispose()
}

fun testZipWith() {
    /**
     * zipWith 是一个操作符，用于将多个 Observable 的发射物按照指定的函数结合在一起，产生一个新的值。
     * zipWith 操作符可以用于将两个或多个 Observable 发射的数据项组合在一起，并且可以定义一个函数来处理这些组合。
     *
     * 注意：如果两个 Observable 的发射物数量不同，zipWith 会以最短的 Observable 为准。也就是说，
     *      如果其中一个 Observable 发射完毕，zipWith 将停止发射新的数据项。
     *
     * 在本例中，定义了两个range，都发送 int 值，在 zipWith 中将两个数据组合为一个 Pair
     *
     * skip：跳过前 N 个数据
     */
    Observable.range(1, 6)
        .zipWith(Observable.range(1, Int.MAX_VALUE)) { t1, t2 ->
            t1 to t2
        }
        .concatMap { Observable.just(it).delay(1, TimeUnit.SECONDS) }
        .skip(2)
        .doOnComplete { Log.d(TAG, "testZipWith: doOnComplete ") }
        .subscribe {
            Log.d(TAG, "testZipWith: subscribe $it")
        }.dispose()
}

fun testConcatMap() {
    /**
     * 用于将一个 Observable 的每个发射物映射成一个 Observable，并将这些 Observables 按照顺序连接起来。
     * 这操作符的主要目的是保证发射的顺序，确保每个 Observable 发射的数据项按照原始的顺序被订阅者接收。
     *
     * 注意：
     *  1、concatMap 与 flatMap 类似，但是 concatMap 保证了发射的顺序，而 flatMap 则可以交错发射。
     *  2、对于 concatMap 中的每个映射后的 Observable，如果有一个 Observable 抛出错误，则整个序列会立即终止，
     *      并且错误会传递给订阅者。
     */
    Observable.just(1, 2, 3, 4, 5)
        .concatMap {
            Observable.just(it).delay(100, TimeUnit.MILLISECONDS) // 模拟耗时操作
        }
        .subscribe()
        .dispose()
}

fun testCombineLatest() {
    /**
     * combineLatest可以组合多个数据源，多个 Observables 中的任何一个发射数据时，结合最近发射的数据项，
     * 通过一个函数组合它们并发射出去。多用于数据联动的场景
     *
     * 参数：
     *   combineLatest 接收多个 Observables 作为参数，以及一个函数 combiner 用于组合这些 Observables 最近发射的数据。
     *   combiner 函数接收每个 Observable 最近发射的数据项，并根据给定的逻辑生成一个新的数据项。（监听数据应该在： subscribe）
     *
     * 注意：
     *  1、combineLatest 操作符会在任何一个 Observable 发射数据时，结合所有 Observables 最近发射的数据项。因此，
     *     如果其中一个 Observable 发射了多个数据项而另一个没有发射新的数据，combineLatest 会使用最新的那个数据项来组合。
     *  2、如果有一个 Observable 没有发射任何数据，combineLatest 将不会发射任何数据项。
     */
    val numbers = Observable.just(1, 2, 3, 4)
    val words = Observable.just("One", "Two", "Three", "Four", "Five")

    Observable.combineLatest(numbers, words) { number, word -> "$number - $word" }
        .subscribe { result ->
            Log.d(TAG, "testCombineLatest: $result")
        }.dispose()
}

fun testAmbArray() {
    /**
     * ambArray 用于从 多个 Observable 中选择第一个发射数据的 Observable，并只订阅这个 Observable。
     * "amb" 是 "ambitious"（有野心的）的缩写，表示它会选择第一个发射数据的 Observable，并且忽略其他的 Observables。
     *
     * 注意：
     *  1、ambArray 会选择第一个发射数据的 Observable，并且只会订阅这个 Observable，忽略其他的 Observables。
     *  2、如果多个 Observables 中有一个或多个同时发射第一个数据，ambArray 会选择其中之一，而忽略其他的。
     */
    val observable1 = Observable.just(1, 2, 3).delay(1, TimeUnit.SECONDS)
    val observable2 = Observable.just(4, 5, 6)

    /**
     * 此例中，只会监听 observable2 的数据
     */
    Observable.ambArray(observable1, observable2).subscribe { result: Int? ->
            Log.d(TAG, "testAmbArray: $result")
        }.dispose()
}