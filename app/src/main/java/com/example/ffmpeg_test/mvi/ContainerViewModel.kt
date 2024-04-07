package com.example.ffmpeg_test.mvi

import android.util.Log
import com.example.ffmpeg_test.mvi.base.BaseViewModel
import com.example.ffmpeg_test.mvi.base.Size
import com.example.ffmpeg_test.mvi.base.UserIntent
import com.example.ffmpeg_test.mvi.base.UserState
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class ContainerViewModel: BaseViewModel<BehaviorSubject<UserState>, UserIntent , Observable<UserState>, UserState>() {

    override val state: BehaviorSubject<UserState> = BehaviorSubject.createDefault(UserState())

    override fun getStateObservable(): Observable<UserState> {
        return state.hide()
    }

    override fun getMyState(): UserState {
        return state.value!!
    }

    override fun dispatchIntent(intent: UserIntent) {
        Log.d(TAG, "dispatchIntent: ${intent.name}")

        intent.checkToBig(state.value!!).also {
            state.onNext(it)
        }

        val localState = when(intent) {
            UserIntent.BigMap -> {
                intent.checkToBig(state.value!!)
            }
            UserIntent.SmallMap -> {
                state.value!!.copy(mapState = Size.SMALL)
            }
            UserIntent.BigAssistant -> {
                intent.checkToBig(state.value!!)
            }
            UserIntent.SmallAssistant -> {
                state.value!!.copy(assistantState = Size.SMALL)
            }
            UserIntent.BigAttitude -> {
                intent.checkToBig(state.value!!)
            }
            UserIntent.SmallAttitude -> {
                state.value!!.copy(attitudeSTate = Size.SMALL)
            }
        }
        state.onNext(localState)
    }

    companion object {
        private const val TAG = "ContainerViewModel"
    }
}
