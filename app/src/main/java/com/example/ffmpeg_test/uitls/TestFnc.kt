package com.example.ffmpeg_test.uitls

import android.util.Log
import com.example.base.router.TomRouter
import com.example.base.router.TomRouterKey
import io.reactivex.rxjava3.disposables.CompositeDisposable

private const val TAG = "testRouter"
private val compositeDisposable = CompositeDisposable()
fun testRouter() {
    TomRouter.getInstance().putInt(TomRouterKey.TEST, 100)
    Log.d(TAG, "testRouter: ${TomRouter.getInstance().getInt(TomRouterKey.TEST)}")

    compositeDisposable.add(TomRouter.getInstance().getObservable(TomRouterKey.TEST)
        .subscribe {
            Log.d(TAG, "testRouter: 2 $it")
        })
}