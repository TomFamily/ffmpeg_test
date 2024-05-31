package com.example.base.router

import android.os.Bundle
import android.util.Log
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.Subject
import java.util.concurrent.atomic.AtomicReference

class TomRouter private constructor() {

    private val observableMap: MutableMap<String, Subject<*>> = HashMap()
//     private val observableMap: MutableMap<String, Subject<Any>> = HashMap()

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

    @Synchronized
    fun <T : Any> getObservable(key: String): Observable<T> {
        return if (observableMap.containsKey(key)) {
            observableMap[key]?.hide() as Observable<T>
        } else {
            val ob = BehaviorSubject.create<T>()
            observableMap[key] = ob
            ob.hide()
        }
    }

    companion object {
        private const val TAG = "TomRouter"
        private var instant: AtomicReference<TomRouter> = AtomicReference()
        private var bundle = Bundle()
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