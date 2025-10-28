// AudioTrackMetronomeViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.util.Log
import android.view.Choreographer
import androidx.lifecycle.AndroidViewModel
import com.sunrise.blog.audio.AudioTrackManager
import com.sunrise.blog.model.MetronomeData
import com.sunrise.blog.model.StepSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

class AudioTrackMetronomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _metronomeData = MutableStateFlow(MetronomeData())
    val metronomeData: StateFlow<MetronomeData> = _metronomeData.asStateFlow()

    private var choreographer: Choreographer? = null
    private var frameCallback: Choreographer.FrameCallback? = null
    private val audioManager = AudioTrackManager()

    // 精确计时相关变量
    private var startTime: Long = 0L
    private var nextBeatTime: Long = 0L
    private var totalBeats: Int = 0
    private var interval: Long = 0L
    private var accumulatedError: Long = 0L

    companion object {
        private const val TAG = "AudioTrackMetronome"
        private const val NANOS_PER_MINUTE = 60_000_000_000L
        private const val NANOS_PER_MILLIS = 1_000_000L
    }

    init {
        initializeAudioTrack()
        choreographer = Choreographer.getInstance()
    }

    private fun initializeAudioTrack() {
        audioManager.initialize()
        audioManager.setVolume(_metronomeData.value.volume)
        Log.d(TAG, "AudioTrack initialized: ${audioManager.isInitialized()}")
    }

    fun startMetronome() {
        stopMetronome()

        if (!audioManager.isInitialized()) {
            Log.e(TAG, "AudioTrack not initialized, cannot start metronome")
            return
        }

        val currentData = _metronomeData.value
        val tempo = currentData.tempo

        // 使用纳秒级精度计算间隔
        interval = NANOS_PER_MINUTE / tempo

        // 重置计时和计数
        startTime = System.nanoTime()
        nextBeatTime = startTime + interval
        totalBeats = 0
        accumulatedError = 0L

        Log.d(TAG, "开始步频器 - 目标步频: $tempo 步/分钟, 纳秒间隔: $interval")

        // 更新状态
        _metronomeData.value = currentData.copy(
            isRunning = true,
            beatCount = 0,
            currentStep = StepSide.LEFT,
            startTime = System.currentTimeMillis(),
            actualTempo = 0.0
        )

        // 立即播放第一个声音
        audioManager.playTick()
        totalBeats++

        // 使用 Choreographer 实现高精度定时
        frameCallback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                checkBeat(frameTimeNanos)
                choreographer?.postFrameCallback(this)
            }
        }
        choreographer?.postFrameCallback(frameCallback!!)
    }

    private fun checkBeat(frameTimeNanos: Long) {
        if (frameTimeNanos >= nextBeatTime) {
            val currentData = _metronomeData.value

            // 计算实际间隔和误差
            val actualInterval = frameTimeNanos - (nextBeatTime - interval)
            val error = actualInterval - interval
            accumulatedError += error

            Log.v(TAG, "节拍 ${currentData.beatCount + 1} - 目标间隔: ${interval/NANOS_PER_MILLIS}ms, 实际间隔: ${actualInterval/NANOS_PER_MILLIS}ms, 单次误差: ${error/NANOS_PER_MILLIS}ms, 累积误差: ${accumulatedError/NANOS_PER_MILLIS}ms")

            // 动态调整下一个节拍时间，补偿累积误差
            nextBeatTime += interval - (accumulatedError / 8) // 补偿累积误差
            accumulatedError = accumulatedError * 7 / 8 // 保留12.5%的累积误差用于后续补偿

            // 播放声音和更新状态
            audioManager.playTick()
            totalBeats++

            // 切换左右脚
            val newStep = if (currentData.currentStep == StepSide.LEFT)
                StepSide.RIGHT
            else
                StepSide.LEFT

            // 定期检查实际步频
            if (totalBeats % 10 == 0) {
                val durationMinutes = (System.nanoTime() - startTime).toDouble() / NANOS_PER_MINUTE
                val actualTempo = if (durationMinutes > 0) totalBeats / durationMinutes else 0.0

                Log.d(TAG, "实际步频检查 - 节拍: $totalBeats, 运行时间: ${"%.1f".format(durationMinutes*60)}秒, 实际步频: ${"%.2f".format(actualTempo)} 步/分钟, 目标步频: ${currentData.tempo} 步/分钟, 偏差: ${"%.2f".format(actualTempo - currentData.tempo)}")

                _metronomeData.value = currentData.copy(actualTempo = actualTempo)
            }

            _metronomeData.value = currentData.copy(
                currentStep = newStep,
                beatCount = currentData.beatCount + 1
            )
        }
    }

    fun stopMetronome() {
        frameCallback?.let {
            choreographer?.removeFrameCallback(it)
            Log.d(TAG, "已移除 Choreographer 回调")
        }
        frameCallback = null

        // 计算最终的实际步频
        val endTime = System.nanoTime()
        val durationMinutes = (endTime - startTime).toDouble() / NANOS_PER_MINUTE
        val finalActualTempo = if (durationMinutes > 0) totalBeats / durationMinutes else 0.0

        Log.d(TAG, "停止步频器 - 总节拍数: $totalBeats, 运行时间: ${"%.2f".format(durationMinutes*60)}秒, 实际步频: ${"%.3f".format(finalActualTempo)} 步/分钟, 最终累积误差: ${accumulatedError/NANOS_PER_MILLIS}ms")

        _metronomeData.value = _metronomeData.value.copy(
            isRunning = false,
            actualTempo = finalActualTempo
        )
    }

    fun setTempo(newTempo: Int) {
        val tempo = max(40, min(240, newTempo))
        Log.d(TAG, "设置目标步频: $tempo 步/分钟")
        _metronomeData.value = _metronomeData.value.copy(tempo = tempo)

        if (_metronomeData.value.isRunning) {
            startMetronome()
        }
    }

    fun increaseTempo() {
        setTempo(_metronomeData.value.tempo + 5)
    }

    fun decreaseTempo() {
        setTempo(_metronomeData.value.tempo - 5)
    }

    fun setVolume(newVolume: Int) {
        val volume = max(0, min(100, newVolume))
        _metronomeData.value = _metronomeData.value.copy(volume = volume)
        audioManager.setVolume(volume)
        Log.d(TAG, "设置音量: $volume")
    }

    override fun onCleared() {
        super.onCleared()
        stopMetronome()
        audioManager.cleanup()
        choreographer = null
        Log.d(TAG, "ViewModel 已清理")
    }
}