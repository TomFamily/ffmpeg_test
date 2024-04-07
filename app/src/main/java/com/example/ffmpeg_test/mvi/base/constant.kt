package com.example.ffmpeg_test.mvi.base

import android.util.Log

private const val TAG = "constant"

enum class Size {
    BIG,
    SMALL
}

/**
 * 三个中只能有一个是big，可以都为 small
 */
data class UserState(val mapState: Size = Size.BIG, val attitudeSTate: Size = Size.SMALL, val assistantState: Size = Size.SMALL) {
    fun isBigMap(): Boolean = this.mapState == Size.BIG
    fun isBigAttitude(): Boolean = this.attitudeSTate == Size.BIG
    fun isBigAssistant(): Boolean = this.assistantState == Size.BIG
}

sealed class UserIntent(val name: String) {

    object BigMap: UserIntent(name = "BigMap")
    object SmallMap: UserIntent(name = "SmallMap")

    object BigAttitude: UserIntent(name = "BigAttitude")
    object SmallAttitude: UserIntent(name = "SmallAttitude")

    object BigAssistant: UserIntent(name = "BigAssistant")
    object SmallAssistant: UserIntent(name = "SmallAssistant")

    fun checkToBig(state: UserState): UserState {
        when(this) {
            BigMap -> {
                var result = if (state.attitudeSTate == Size.BIG) state.copy(attitudeSTate = Size.SMALL) else state
                result = if (result.assistantState == Size.BIG) result.copy(assistantState = Size.SMALL) else result
                result = if (result.mapState != Size.BIG) result.copy(mapState = Size.BIG) else result
                return result
            }
            BigAttitude -> {
                var result = if (state.mapState == Size.BIG) state.copy(mapState = Size.SMALL) else state
                result = if (result.assistantState == Size.BIG) result.copy(assistantState = Size.SMALL) else result
                result = if (result.attitudeSTate != Size.BIG) result.copy(attitudeSTate = Size.BIG) else result
                return result
            }
            BigAssistant -> {
                var result = if (state.mapState == Size.BIG) state.copy(mapState = Size.SMALL) else state
                result = if (result.attitudeSTate == Size.BIG) result.copy(attitudeSTate = Size.SMALL) else result
                result = if (result.assistantState != Size.BIG) result.copy(assistantState = Size.BIG) else result
                return result
            }
            else -> {
                Log.d(TAG, "changeToBig: $this is not Big state")
            }
        }
        return state
    }
}