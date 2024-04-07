package com.example.ffmpeg_test.mvi.base

import androidx.lifecycle.ViewModel

abstract class BaseViewModel<S, T, R, D> : ViewModel() {

    abstract val state: S

    abstract fun dispatchIntent(intent: T)

    abstract fun getStateObservable(): R

    abstract fun getMyState(): D
}
