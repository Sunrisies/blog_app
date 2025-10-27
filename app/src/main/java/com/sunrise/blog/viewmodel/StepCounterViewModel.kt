// StepCounterViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.AndroidViewModel
import com.sunrise.blog.model.StepCounterData
import com.sunrise.blog.model.StepCounterSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val _stepData = MutableStateFlow(StepCounterData())
    val stepData: StateFlow<StepCounterData> = _stepData.asStateFlow()

    private val _settings = MutableStateFlow(StepCounterSettings())
    val settings: StateFlow<StepCounterSettings> = _settings.asStateFlow()

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var vibrator: Vibrator

    // 声音播放器
    private var mediaPlayer: MediaPlayer? = null
    private var isSoundPrepared = false

    // 步数检测相关变量
    private var stepCount = 0
    private var lastStepTime = 0L
    private var lastAlertTime = 0L // 防止声音频繁播放
    private val stepTimes = mutableListOf<Long>()
    private val ACCELERATION_THRESHOLD = 11.0

    // 蓝牙相关变量（简化实现）
    private var isBluetoothConnected = false

    init {
        initSensors()
        prepareSound()
    }

    private fun initSensors() {
        sensorManager = getApplication<Application>().getSystemService(SensorManager::class.java)
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        vibrator = getApplication<Application>().getSystemService(Vibrator::class.java)
        val audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 调大媒体音量
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
        // 注册传感器监听 - 使用兼容的采样率
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private fun prepareSound() {
        try {
            // 尝试加载自定义声音文件
            val context = getApplication<Application>()
            val soundResource = context.resources.getIdentifier("pace_alert", "raw", context.packageName)

            if (soundResource != 0) {
                mediaPlayer = MediaPlayer.create(context, soundResource)
                isSoundPrepared = true
            } else {
                // 如果自定义声音文件不存在，使用系统通知音
                // 我们将在播放时动态创建 MediaPlayer
                isSoundPrepared = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            isSoundPrepared = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            detectStep(event.values[0], event.values[1], event.values[2])
        }
    }

    private fun detectStep(x: Float, y: Float, z: Float) {
        if (!_stepData.value.isRunning) return

        val acceleration = sqrt((x * x + y * y + z * z).toDouble())

        if (acceleration > ACCELERATION_THRESHOLD) {
            val currentTime = System.currentTimeMillis()

            // 防止连续计数，设置最小间隔300ms
            if (currentTime - lastStepTime > 300) {
                stepCount++
                lastStepTime = currentTime
                stepTimes.add(currentTime)

                // 清理超过1分钟的步数记录
                stepTimes.removeAll { it < currentTime - 60000 }

                updateStepData()
                checkPaceAlert()
            }
        }
    }

    private fun updateStepData() {
        val currentData = _stepData.value

        // 计算当前步频（基于最近1分钟的步数）
        val currentPace = stepTimes.size

        // 计算距离和卡路里（简单估算）
        val distance = stepCount * 0.7 / 1000.0 // 每步0.7米，转换为公里
        val calories = stepCount * 0.04 // 每步0.04卡路里

        _stepData.value = currentData.copy(
            steps = stepCount,
            distance = distance,
            calories = calories,
            currentPace = currentPace
        )
    }

    private fun checkPaceAlert() {
        val currentData = _stepData.value
        val targetPace = currentData.targetPace
        val currentPace = currentData.currentPace
        val currentTime = System.currentTimeMillis()

        // 防止声音频繁播放，设置最小间隔2秒
        if (currentTime - lastAlertTime < 2000) return

        // 当当前步频接近目标步频时给出提示
        if (currentPace in (targetPace - 5)..(targetPace + 5) && currentPace > 0) {
            lastAlertTime = currentTime

            // 震动提示
            if (_settings.value.enableVibration && vibrator.hasVibrator()) {
                try {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(100)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // 声音提示
            if (_settings.value.enableSound) {
                playPaceSound()
            }

            // 蓝牙提示（简化实现）
            if (_settings.value.enableBluetooth && isBluetoothConnected) {
                // 这里可以添加通过蓝牙设备播放提示的逻辑
            }
        }
    }

    private fun playPaceSound() {
        try {
            if (isSoundPrepared && mediaPlayer != null) {
                // 使用预加载的声音
                mediaPlayer?.let { player ->
                    if (!player.isPlaying) {
                        player.seekTo(0) // 重置到开始位置
                        player.start()
                    }
                }
            } else {
                // 动态创建 MediaPlayer 播放系统通知音
                val context = getApplication<Application>()
                val mediaPlayer = MediaPlayer().apply {
                    setOnCompletionListener { mp ->
                        mp.release()
                    }
                    setOnErrorListener { mp, what, extra ->
                        mp.release()
                        true
                    }
                }

                try {
                    // 使用系统通知音
                    val soundUri = android.provider.Settings.System.DEFAULT_NOTIFICATION_URI
                    mediaPlayer.setDataSource(context, soundUri)
                    mediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_NOTIFICATION)
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                } catch (e: Exception) {
                    e.printStackTrace()
                    mediaPlayer.release()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startRunning() {
        stepCount = 0
        stepTimes.clear()
        lastAlertTime = 0L
        _stepData.value = StepCounterData(
            isRunning = true,
            startTime = System.currentTimeMillis(),
            targetPace = _stepData.value.targetPace
        )
    }

    fun stopRunning() {
        _stepData.value = _stepData.value.copy(isRunning = false)
    }

    fun reset() {
        stepCount = 0
        stepTimes.clear()
        lastAlertTime = 0L
        _stepData.value = StepCounterData(targetPace = _stepData.value.targetPace)
    }

    fun updateTargetPace(newPace: Int) {
        _stepData.value = _stepData.value.copy(targetPace = newPace)
    }

    fun updateSettings(newSettings: StepCounterSettings) {
        _settings.value = newSettings
    }

    fun connectBluetooth() {
        // 简化的蓝牙连接逻辑
        isBluetoothConnected = true
        // 在实际应用中，这里应该实现真正的蓝牙连接逻辑
    }

    fun disconnectBluetooth() {
        isBluetoothConnected = false
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要处理
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}