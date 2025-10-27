// SimpleMetronomeViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.sunrise.blog.model.MetronomeData
import com.sunrise.blog.model.StepSide
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max
import kotlin.math.min

class SimpleMetronomeViewModel(application: Application) : AndroidViewModel(application) {
    private val _metronomeData = MutableStateFlow(MetronomeData())
    val metronomeData: StateFlow<MetronomeData> = _metronomeData.asStateFlow()

    private var handler: Handler? = null
    private var runnable: Runnable? = null
    private var toneGenerator: ToneGenerator? = null
    private var volume: Int = 80 // 音量设置，0-100

    // 计时相关变量
    private var startTime: Long = 0L
    private var lastBeatTime: Long = 0L
    private var totalBeats: Int = 0
    private var tempoCheckCounter: Int = 0

    // 日志标签
    companion object {
        private const val TAG = "Metronome"
    }

    init {
        handler = Handler(Looper.getMainLooper())
        initializeToneGenerator()
    }

    private fun initializeToneGenerator() {
        try {
            // 创建 ToneGenerator，使用音量设置
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, volume)
            Log.d(TAG, "ToneGenerator 初始化成功，音量: $volume")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "ToneGenerator 初始化失败: ${e.message}")
            toneGenerator = null
        }
    }

    fun startMetronome() {
        stopMetronome() // 确保先停止

        val currentData = _metronomeData.value
        val interval = 60000 / currentData.tempo // 计算每个步频的间隔（毫秒）

        // 重置计时和计数
        startTime = System.currentTimeMillis()
        lastBeatTime = startTime
        totalBeats = 0
        tempoCheckCounter = 0

        Log.d(TAG, "开始步频器 - 目标步频: ${currentData.tempo} 步/分钟, 间隔: $interval 毫秒")

        // 立即更新状态
        _metronomeData.value = currentData.copy(
            isRunning = true,
            beatCount = 0,
            currentStep = StepSide.LEFT, // 从左脚开始
            startTime = startTime,
            actualTempo = 0.0
        )

        // 立即播放第一个声音
        playTickSound()
        totalBeats++

        // 使用 Handler 实现定时器
        runnable = object : Runnable {
            override fun run() {
                tick()
                handler?.postDelayed(this, interval.toLong())
            }
        }

        handler?.postDelayed(runnable!!, interval.toLong())
    }

    fun stopMetronome() {
        // 先移除所有回调
        runnable?.let {
            handler?.removeCallbacks(it)
            Log.d(TAG, "已移除定时器回调")
        }
        runnable = null

        // 计算最终的实际步频
        val endTime = System.currentTimeMillis()
        val durationMinutes = (endTime - startTime) / 60000.0
        val finalActualTempo = if (durationMinutes > 0) totalBeats / durationMinutes else 0.0

        Log.d(TAG, "停止步频器 - 总节拍数: $totalBeats, 运行时间: ${(endTime - startTime)/1000}秒, 实际步频: ${"%.1f".format(finalActualTempo)} 步/分钟")

        // 然后更新状态
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
            startMetronome() // 重启以应用新速度
        }
    }

    fun increaseTempo() {
        setTempo(_metronomeData.value.tempo + 5)
    }

    fun decreaseTempo() {
        setTempo(_metronomeData.value.tempo - 5)
    }

    fun setVolume(newVolume: Int) {
        volume = max(0, min(100, newVolume))
        Log.d(TAG, "设置音量: $volume")
        // 重新初始化 ToneGenerator 以应用新音量
        toneGenerator?.release()
        initializeToneGenerator()
    }

    private fun tick() {
        val currentTime = System.currentTimeMillis()
        val currentData = _metronomeData.value

        // 计算实际间隔
        val actualInterval = currentTime - lastBeatTime
        lastBeatTime = currentTime
        totalBeats++

        // 每10个节拍检查一次实际步频
        tempoCheckCounter++
        if (tempoCheckCounter >= 10) {
            val durationMinutes = (currentTime - startTime) / 60000.0
            val actualTempo = if (durationMinutes > 0) totalBeats / durationMinutes else 0.0

            Log.d(TAG, "实际步频检查 - 节拍: $totalBeats, 运行时间: ${"%.1f".format(durationMinutes*60)}秒, 实际步频: ${"%.1f".format(actualTempo)} 步/分钟, 目标步频: ${currentData.tempo} 步/分钟, 偏差: ${"%.1f".format(actualTempo - currentData.tempo)}")

            tempoCheckCounter = 0

            // 更新显示的实际步频
            _metronomeData.value = currentData.copy(actualTempo = actualTempo)
        }

        // 切换左右脚
        val newStep = if (currentData.currentStep == StepSide.LEFT)
            StepSide.RIGHT
        else
            StepSide.LEFT

        playTickSound()

        _metronomeData.value = currentData.copy(
            currentStep = newStep,
            beatCount = currentData.beatCount + 1
        )

        // 记录每个节拍的详细信息
        Log.v(TAG, "节拍 ${currentData.beatCount + 1} - 目标间隔: ${60000/currentData.tempo}ms, 实际间隔: ${actualInterval}ms, 差值: ${actualInterval - 60000/currentData.tempo}ms")
    }

    private fun playTickSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_DTMF_0, 80) // 增加音长到80毫秒
            Log.v(TAG, "播放滴答声")
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "播放声音失败: ${e.message}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMetronome()
        toneGenerator?.release()
        toneGenerator = null
        handler = null
        Log.d(TAG, "ViewModel 已清理")
    }
}