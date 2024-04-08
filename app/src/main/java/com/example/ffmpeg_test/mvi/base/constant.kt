package com.example.ffmpeg_test.mvi.base

import android.util.Log

private const val TAG = "constant"

enum class Size {
    BIG,
    MID,
    SMALL
}

/**
 * 三个中只能有一个是big / mid (其他为small），可以都为 small
 */
sealed class UserState(val mapState: Size = Size.BIG, val attitudeSTate: Size = Size.SMALL, val assistantState: Size = Size.SMALL) {
    object BigMap: UserState(mapState = Size.BIG, attitudeSTate = Size.SMALL, assistantState = Size.SMALL)
    object BigAttitude: UserState(mapState = Size.SMALL, attitudeSTate = Size.BIG, assistantState = Size.SMALL)
    object BigAssistant: UserState(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.BIG)

    object MidMap: UserState(mapState = Size.MID, attitudeSTate = Size.SMALL, assistantState = Size.SMALL)
    object MidAttitude: UserState(mapState = Size.SMALL, attitudeSTate = Size.MID, assistantState = Size.SMALL)
    object MidAssistant: UserState(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.MID)

    object AllSmall: UserState(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.SMALL)
}

sealed class UserIntent(val name: String) {

    object BigMap: UserIntent(name = "BigMap")
    object MidMap: UserIntent(name = "MidMap")
    object SmallMap: UserIntent(name = "SmallMap")

    object BigAttitude: UserIntent(name = "BigAttitude")
    object MidAttitude: UserIntent(name = "MidAttitude")
    object SmallAttitude: UserIntent(name = "SmallAttitude")

    object BigAssistant: UserIntent(name = "BigAssistant")
    object MidAssistant: UserIntent(name = "MidAssistant")
    object SmallAssistant: UserIntent(name = "SmallAssistant")

//data class UserState(val mapState: Size = Size.BIG, val attitudeSTate: Size = Size.SMALL, val assistantState: Size = Size.SMALL) {
//    fun isBigMap(): Boolean = this.mapState == Size.BIG
//    fun isBigAttitude(): Boolean = this.attitudeSTate == Size.BIG
//    fun isBigAssistant(): Boolean = this.assistantState == Size.BIG
//}

//    fun checkToBig(state: UserState): UserState {
//        when(this) {
//            BigMap -> {
//                var result = if (state.attitudeSTate == Size.BIG) state.copy(attitudeSTate = Size.SMALL) else state
//                result = if (result.assistantState == Size.BIG) result.copy(assistantState = Size.SMALL) else result
//                result = if (result.mapState != Size.BIG) result.copy(mapState = Size.BIG) else result
//                return result
//            }
//            BigAttitude -> {
//                var result = if (state.mapState == Size.BIG) state.copy(mapState = Size.SMALL) else state
//                result = if (result.assistantState == Size.BIG) result.copy(assistantState = Size.SMALL) else result
//                result = if (result.attitudeSTate != Size.BIG) result.copy(attitudeSTate = Size.BIG) else result
//                return result
//            }
//            BigAssistant -> {
//                var result = if (state.mapState == Size.BIG) state.copy(mapState = Size.SMALL) else state
//                result = if (result.attitudeSTate == Size.BIG) result.copy(attitudeSTate = Size.SMALL) else result
//                result = if (result.assistantState != Size.BIG) result.copy(assistantState = Size.BIG) else result
//                return result
//            }
//            else -> {
//                Log.d(TAG, "changeToBig: $this is not Big state")
//            }
//        }
//        return state
//    }
}