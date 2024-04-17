package com.example.base.rxjava

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.base.MyApplication
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit


/**
 *Created by arno.yang
 *Created on 2023/10/25 10:47
 *PackageName com.example.jni_test.test
 */

private const val TAG = "TestRxjava"

fun main() {
    // testDefer()
    testDefer2()
}

fun testRxjava(context: Context) {
    testScheduler(context)
    testDelaySubscription()
    testAndThen()
    testFlatMap()
    testThread()
    testDebounce()
}

/**
 * scan 可以拿到前一个发送的值
 * 用途：将前一个值与当前值 进行 结合处理
 */
private fun testScan() {
    Observable.just(1,2,3,4,6)
        .scan { t1, t2 ->
            t2 + t1
        }.subscribe {
            Log.d(TAG, "testScan: $it")
        }.dispose()
}

/**
 * reduce 操作符也是 RxJava 中的一种转换操作符，它可以将 Observable 发射的数据项按照指定的规则进行聚合操作，
 * 最终只发射一个数据项给观察者，即聚合的最终结果。
 */
private fun testReduce() {
    Observable.just(1,2,3,4,6)
        .reduce { t1, t2 ->
            t1 + t2
        }.subscribe {
            Log.d(TAG, "testReduce: $it")
        }.dispose()
}

/**
 * count 操作符是 RxJava 中的一个转换操作符，它用于统计 Observable 发射的数据项的数量，
 * 并将统计结果作为一个单独的数据项发送给观察者。
 */

/**
 * sequenceEqual 操作符是 RxJava 中的一个条件和布尔型操作符，用于判断两个 Observable
 * 是否发射相同的数据序列，以及这些数据是否按相同的顺序发射。它会比较两个 Observable 发射的每一个数据项，
 * 如果两个 Observable 的数据序列完全相同，则 sequenceEqual 操作符会发射一个布尔值 true，否则会发射一个布尔值 false。
 */

private fun testSequenceEqual() {
    val observable1 = Observable.just(1, 2, 3, 4, 5)
    val observable2 = Observable.just(1, 2, 3, 4, 5)

    Observable.sequenceEqual(observable1, observable2)
        .subscribe { equal -> println("Are sequences equal? $equal") }.dispose()
// 输出：
// Are sequences equal? true
}

private fun testDebounce() {
    /**
     * debounce 是 RxJava 中的一个操作符，用于过滤掉发射速率过快的数据项，只保留最后一个数据项。
     * 具体来说，debounce 操作符会等待一段指定的时间，在 这段时间内 如果没有新的数据发射，
     * 则会将最后一个数据项发射出去；如果在这段时间内有新的数据发射，则会重新开始计时。这样可以确保只有在数据
     * 流停止发射一段时间后，才会将最后一个数据项发射出去，从而实现对数据流的控制和过滤。
     */
    Observable.just(1,2,3,4,6)
        .debounce(100, TimeUnit.MILLISECONDS).subscribe().dispose()
}

private fun testWindow() {
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
        .doOnNext {  }
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

    val observable1: Observable<Int> = Observable.just(1, 2, 3)
    val observable2 = Observable.just(4, 5, 6)

    Observable.concat(observable1, observable2).subscribe {
        Log.d(TAG, "testConcatWith concat: $it")
    }.dispose()

    observable1.concatWith(observable2).subscribe {
        Log.d(TAG, "testConcatWith concatWith: $it")
    }.dispose()
}

/**
 * 1、多个 subscribeOn，会以距离 Observable 最近的一个为准（只会有一个生效）
 * 2、subscribeOn() 方法会影响 第一个 observeOn() 之前的流程
 * 3、observeOn() 方法会影响该操作符之后的代码。
 * 4、代码中存在多次调用 subscribeOn()，但只有第一个调用起作用；但 observeOn() 方法 每次调用都会生效
 */
private fun testThread() {
    val observable = Observable.just(false)
//         .subscribeOn(Schedulers.io())
        .subscribeOn(AndroidSchedulers.mainThread())
        .doOnNext { Log.d(TAG, "testThread1: ${Thread.currentThread()}") }
        .doOnNext { Log.d(TAG, "testThread2: ${Thread.currentThread()}") }
        .observeOn(AndroidSchedulers.mainThread())
        .buffer(5)
        .doOnNext { it -> Log.d(TAG, "testThread3: ${Thread.currentThread()}") }
        .doOnNext { Log.d(TAG, "testThread4: ${Thread.currentThread()}") }
        // .subscribeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .doOnNext { Log.d(TAG, "testThread5: ${Thread.currentThread()}") }
        .subscribeOn(Schedulers.newThread())
        .subscribe()

    BehaviorSubject.createDefault(3).toSerialized()

    val observable2 = Observable.create(object : ObservableOnSubscribe<String> {
        override fun subscribe(emitter: ObservableEmitter<String>) {
            emitter.onNext("ri")
        }
    })

    RxView.clicks(View(MyApplication.getContext()))

    val observer2: Observer<String> = object : Observer<String> {
        private lateinit var dis: Disposable
        override fun onSubscribe(d: Disposable) {
            dis = d
        }

        override fun onError(e: Throwable) {
        }

        override fun onComplete() {
        }

        override fun onNext(t: String) {
            if (t == "dis") {
                dis.dispose()
            }
        }
    }
    observable2
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(observer2)
}

private fun testEmpty() {
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
                .flatMap { Observable.just("123") }
                .observeOn(AndroidSchedulers.mainThread())
        }

    Observable.just(3).blockingFirst()

    val subject = BehaviorSubject.createDefault(3)

    /**
     * distinctUntilChanged false 可过
     * filter true 可过
     */
    Observable.just(1, 2, 3, 4, 5)
        .distinctUntilChanged { t1, t2 -> true }
        .compose(customOperator!!)
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

private fun testDefer2() {
    println("testDefer2")
    val c = Observable.fromIterable(object : Iterable<Int> {
        override fun iterator(): Iterator<Int> {
            println("iterator1")
            return getList("fromIterable iterator").iterator()
        }
    })
    val a = Observable.fromIterable(getList("fromIterable"))
    val b = Observable.defer {
        Observable.fromIterable(getList("fromIterable defer"))
    }
}

private fun getList(string: String): List<Int> {
    println(string)
    listOf(1,3,4,5).stream()
    return listOf(1,3,4,5)
}

fun testAndThen() {
    /**
     * andThen 可以组合两个 Completable，在前一个 Completable 执行完成后，才会再执行第二个 Completable
     * 在本例中，将 completable1、completable2 组合，在 completable1 执行完后，completable2才会执行
     */
    val completable1 = Completable.create {
        Log.d(TAG, "testAndThen: completable1")
        it.onComplete()
    }

    val completable2 = Completable.create {
        Log.d(TAG, "testAndThen: completable2")
        it.onComplete()
    }

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

fun testIntervalRange() {
    /**
     * @param initialDelay 发送第一个值的延迟
     * @param period 发送两个值之间的时间间隔
     */
    Observable.intervalRange(0, 10, 300, 2000, TimeUnit.MILLISECONDS)
        .cast(Int::class.java)
        .subscribe {
            Log.d(TAG, "testIntervalRange: $it")
        }.dispose()
}

private fun testScheduler(context: Context) {
    /**
     * [Schedulers] 可以返回一个用于在 I/O 操作上执行任务的调度器。任务可以放在 Schedulers.io() 所代表的线程池中执行
     * RxJava 中的调度器 [Schedulers] 并不是直接映射到单个线程，而是通过线程池来管理任务的执行。通过 Schedulers.io() 执行
     * 的任务，会被 Schedulers.io() 线程池中的某个线程执行，但具体在哪个线程执行，取决于调度器和线程池的实现
     */
    Log.d(TAG, "testScheduler: ${Thread.currentThread()}")
    Schedulers.io().scheduleDirect(
        {
            Log.d(TAG, "testScheduler Schedulers1: ${Thread.currentThread()} ${Thread.currentThread() == Looper.getMainLooper().thread}")
            Handler(Looper.getMainLooper()).post {
                // Toast.makeText(context, "你好啊", Toast.LENGTH_SHORT).show()
            }
            AndroidSchedulers.mainThread().scheduleDirect {
                Log.d(TAG, "testScheduler Schedulers2: ${Thread.currentThread()} ${Thread.currentThread() == Looper.getMainLooper().thread}")
                // Toast.makeText(context, "你好啊", Toast.LENGTH_SHORT).show()
            }
        },
        1,
        TimeUnit.SECONDS
    )

    /**
     * 需要执行计算密集型的操作，可以考虑使用 [Schedulers.computation()]
     */
}

/**
 * RxJava 中，timestamp 操作符用于将原始的数据项转换为包含时间戳信息的数据项。
 */
fun testTimestamp() {
    Observable.intervalRange(0, 10, 300, 2000, TimeUnit.MILLISECONDS)
        .timestamp()
        .subscribe {
            /**
             * it.time() 每个数据发送时的时间戳
             */
            Log.d(TAG, "testIntervalRange: ${it.time()} ${it.value()}")
        }.dispose()
}

/**
 * timeInterval 操作符用于将原始的数据项转换为包含时间间隔信息的数据项。这可以帮助你了解两个连续事件之间经过的时间。
 */
fun testTimeInterval() {
    Observable.intervalRange(0, 10, 300, 2000, TimeUnit.MILLISECONDS)
        .timeInterval()
        .subscribe {
            /**
             * it.time() 本数据发送时间距离上一个数据的时间间隔
             */
            Log.d(TAG, "testIntervalRange: ${it.time()} ${it.value()}")
        }.dispose()
}

/**
 * "delaySubscription" 操作符的作用是延迟订阅（Subscription）源 Observable（可观察对象）。
 *
 * 使用场景： 有时候，你可能想要在一定条件下延迟订阅 Observable。这可以是在特定时间点，或者是在某些事件发生后。
 * "delaySubscription" 就是为了解决这类需求而设计的。
 */
private fun testDelaySubscription() {
    Log.d(TAG, "testDelaySubscription: ")
    Observable.just(10)
        .doOnNext { Log.d(TAG, "testDelaySubscription1") }
        .delaySubscription(2, TimeUnit.SECONDS)
        .doOnNext { Log.d(TAG, "testDelaySubscription2") }
        .subscribe {
            Log.d(TAG, "testDelaySubscription: 3")
        }.dispose()
}

private fun testToObservable() {
    /**
     * toObservable()：用于将其他类型的数据流或事件转换成 Observable 对象，如：Single
     */
    Single.just(50).toObservable().subscribe {
        Log.d(TAG, "testToObservable: $it")
    }.dispose()
}