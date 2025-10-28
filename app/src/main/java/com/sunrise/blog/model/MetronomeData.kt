package com.sunrise.blog.model

data class MetronomeData(
    val isRunning: Boolean = false,
    val tempo: Int = 120, // 步频（步/分钟）
    val currentStep: StepSide = StepSide.LEFT, // 当前脚步
    val beatCount: Int = 0, // 节拍计数
    val startTime: Long = 0L, // 开始时间
    val actualTempo: Double = 0.0, // 实际计算的步频
    val volume: Int = 80 // 音量 0-100
)

enum class StepSide {
    LEFT, RIGHT
}