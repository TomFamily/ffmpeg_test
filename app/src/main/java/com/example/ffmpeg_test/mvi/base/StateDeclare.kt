package com.example.ffmpeg_test.mvi.base

/**
 * UI界面状态声明
 * 灵感来源：compose 声明和组合的理念；compose声明式UI，只用调用 compose 为开发者定义的函数间接地对控件进行操作，无需直接的 操控控件
 *         eg：声明 在 状态A 时，控件 control 的样式是 style1， 在 状态B 时，control 的样式是 style2
 *
 * 声明式UI：开发者通过描述UI的外观和行为，而不需要详细指定每个UI元素的创建和操作过程。
 *
 * UI界面状态声明：在编码阶段，明确定义 出界面的各个状态对应的style，在为控件设置/刷新状态时，控件无需判断所处的状态，进而给
 * 自己设置对应的状态；控件直接拿最新的style设置给自己就行
 */

/**
 * 举例：
 * 1、Activity 有 三个主要View：Setting List、Tip List、FPV，三者的显示互斥（只能同时有个一个显示）
 * 2、FPV 包含有：相机参数（大中小模式）、地图、姿态球、辅助影像
 * 3、地图、姿态球、辅助影像各自都有大中小三个状态，且三个中只能有一个是big / mid (其他为small），可以都为 small
 * 4、相机参数 与 （地图、姿态球、辅助影像） 互不影响
 */

data class MyActivity(val activityState: ActivityState, val fpvState: FpvState)

enum class ActivityState(val settingVisibility: Boolean, val tipVisibility: Boolean, val fpvVisibility: Boolean) {
    SettingVisibility(true, false, false),
    TipVisibility(false, true, false),
    FpvVisibility(false, false, true)
}

data class FpvState(val cameraState: Size, val dingState: DingState)

/**
 * 地图、姿态球、辅助影像各自都有大中小三个状态，且三个中只能有一个是big / mid (其他为small），可以都为 small
 */
enum class DingState(val mapState: Size, val attitudeSTate: Size, val assistantState: Size) {
    BigMap(mapState = Size.BIG, attitudeSTate = Size.SMALL, assistantState = Size.SMALL),
    BigAttitude(mapState = Size.SMALL, attitudeSTate = Size.BIG, assistantState = Size.SMALL),
    BigAssistant(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.BIG),
    MidMap(mapState = Size.MID, attitudeSTate = Size.SMALL, assistantState = Size.SMALL),
    MidAttitude (mapState = Size.SMALL, attitudeSTate = Size.MID, assistantState = Size.SMALL),
    MidAssistant(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.MID),
    AllSmall(mapState = Size.SMALL, attitudeSTate = Size.SMALL, assistantState = Size.SMALL),
}

/**
 * 面临问题：
 * 1、状态无法穷居，上面 一共有 63 种状态（ActivityState = 3， cameraState = 3， dingState = 7）
 * 2、因此采用状态组合的方式进行状态声明
 * 3、组合（拆分）规则："平行"控件的状态可以进行拆分（两个控件之间的状态不会相互影响）
 *    eg：cameraState 与 dingState 无依赖关系，所以拆分为两个 enum class 进行状态声明，通过组合的方式完成整个页面的状态声明
 * 4、状态树：表示整个界面的状态；叶子节点负责穷居具体控件的状态，交叉节点/根节点负责 组合 叶子节点的状态
 * 5、交叉节点/根节点节点用：data class， 叶子节点用：enum class
 */

fun main() {
    val state = MyActivity(ActivityState.FpvVisibility, FpvState(Size.SMALL, DingState.AllSmall))
    println("state: $state")
}

