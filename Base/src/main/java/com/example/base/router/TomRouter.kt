package com.example.base.router

import android.os.Bundle
import android.util.Log
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.HashMap

class TomRouter private constructor() {
    @Synchronized
    fun clearData() {
        Log.d(TAG, "clearData")
        observableMap.clear()
        bundle.clear()
    }

    @Synchronized
    fun putInt(key: String, value: Int) {
        bundle.putInt(key, value)
        if (observableMap.containsKey(key)) {
            val ob = observableMap[key] as BehaviorSubject<Int>
            ob.onNext(value)
        } else {
            observableMap[key] = BehaviorSubject.createDefault(value)
        }
    }

    @Synchronized
    fun getInt(key: String): Int {
        return bundle.getInt(key)
    }

    /**
     * todo fly 中在获取数据时 getObservable，是如何知道数据类型的
     */
    @Synchronized
    fun getObservable(key: String): io.reactivex.rxjava3.core.Observable<Any> {
        return if (observableMap.containsKey(key)) {
            observableMap[key]!!.hide()
        } else {
            val ob = BehaviorSubject.create<Any>()
            observableMap[key] = ob
            ob.hide()
        }
    }

    companion object {
        private const val TAG = "TomRouter"
        private var instant: AtomicReference<TomRouter> = AtomicReference()
        private var bundle = Bundle()
        private val observableMap: MutableMap<String, BehaviorSubject<Any>> = HashMap()
        // var synchronizedMap: Map<String, BehaviorSubject<Any>> = Collections.synchronizedMap(HashMap())

        @Synchronized
        fun getInstance(): TomRouter {
            if (instant.get() == null) {
                instant.set(TomRouter())
            }
            return instant.get()
        }
    }
}