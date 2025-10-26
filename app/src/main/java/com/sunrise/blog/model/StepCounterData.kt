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
data class StepCounterData(
    val steps: Int = 0,
    val isRunning: Boolean = false,
    val startTime: Long = 0L,
    val calories: Double = 0.0,
    val distance: Double = 0.0
)