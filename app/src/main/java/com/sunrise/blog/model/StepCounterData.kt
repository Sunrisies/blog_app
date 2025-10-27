// StepCounterData.kt
package com.sunrise.blog.model

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//@Immutable
//data class StepCounterData(
//    val totalSteps: Int = 0,
//    val currentSessionSteps: Int = 0,
//    val calories: Double = 0.0,
//    val distance: Double = 0.0,
//    val pace: Double = 0.0, // 分钟/公里
//    val isRunning: Boolean = false,
//    val startTime: Long = 0L,
//    val lastStepTime: Long = 0L
//)
//
//data class StepCounterSettings(
//    val stepFrequency: Int = 120, // 目标步频（步/分钟）
//    val weight: Double = 70.0, // 用户体重（kg）
//    val strideLength: Double = 0.7, // 步长（米）
//    val enableBluetooth: Boolean = true,
//    val keepScreenOn: Boolean = false
//)
// StepCounterData.kt
// StepCounterData.kt

data class StepCounterData(
    val steps: Int = 0,
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val calories: Double = 0.0,
    val distance: Double = 0.0,
    val currentPace: Int = 0, // 当前步频（步/分钟）
    val targetPace: Int = 160 // 目标步频
)

data class StepCounterSettings(
    val targetPace: Int = 160,
    val enableSound: Boolean = true,
    val enableVibration: Boolean = true,
    val enableBluetooth: Boolean = false
)

// MetronomeData.kt

data class MetronomeData(
    val isRunning: Boolean = false,
    val tempo: Int = 120, // 步频（步/分钟）
    val currentStep: StepSide = StepSide.LEFT, // 当前脚步
    val beatCount: Int = 0, // 节拍计数
    val startTime: Long = 0L, // 开始时间
    val actualTempo: Double = 0.0 // 实际计算的步频
)


enum class StepSide {
    LEFT, RIGHT
}