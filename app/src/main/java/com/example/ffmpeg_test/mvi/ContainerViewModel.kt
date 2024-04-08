package com.example.ffmpeg_test.mvi

import android.util.Log
import com.example.ffmpeg_test.mvi.base.BaseViewModel
import com.example.ffmpeg_test.mvi.base.UserIntent
import com.example.ffmpeg_test.mvi.base.UserState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ContainerViewModel: BaseViewModel<BehaviorSubject<UserState>, UserIntent , Observable<UserState>, UserState>() {

    override val state: BehaviorSubject<UserState> = BehaviorSubject.createDefault(UserState.AllSmall)

    override fun getStateObservable(): Observable<UserState> {
        return state.hide()
    }

    override fun getMyState(): UserState {
        return state.value!!
    }

    override fun dispatchIntent(intent: UserIntent) {
        Log.d(TAG, "dispatchIntent: ${intent.name}")

        val localState = when(intent) {
            UserIntent.BigMap -> UserState.BigMap
            UserIntent.MidMap -> UserState.MidMap
            UserIntent.SmallMap -> UserState.AllSmall
            UserIntent.BigAssistant -> UserState.BigAssistant
            UserIntent.MidAssistant -> UserState.MidAssistant
            UserIntent.BigAttitude -> UserState.BigAttitude
            UserIntent.MidAttitude -> UserState.MidAttitude
            UserIntent.SmallMap, UserIntent.SmallAssistant, UserIntent.SmallAttitude -> UserState.AllSmall
        }
        state.onNext(localState)
    }

    companion object {
        private const val TAG = "ContainerViewModel"
    }
}
