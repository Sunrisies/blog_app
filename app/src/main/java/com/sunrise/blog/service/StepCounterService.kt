// StepCounterService.kt
package com.sunrise.blog.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.*
import androidx.core.app.NotificationCompat
import com.sunrise.blog.MainActivity
import com.sunrise.blog.R
import com.sunrise.blog.model.StepCounterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*
import kotlin.math.sqrt

class StepCounterService : Service(), SensorEventListener {
    private val binder = StepCounterBinder()
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var vibrator: Vibrator

    private val _stepData = MutableStateFlow(StepCounterData())
    val stepData: StateFlow<StepCounterData> = _stepData

    // 步数检测相关变量
    private var stepCount = 0
    private var lastStepTime = 0L
    private val stepTimes = LinkedList<Long>()
    private val ACCELERATION_THRESHOLD = 11.0

    inner class StepCounterBinder : Binder() {
        fun getService(): StepCounterService = this@StepCounterService
    }

    override fun onBind(intent: Intent): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        initializeComponents()
        startForegroundService()
    }

    private fun initializeComponents() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        // 调大媒体音量
        audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND)
        // 修复：使用兼容的传感器采样率，避免 HIGH_SAMPLING_RATE_SENSORS 权限问题
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME // 改为 GAME 延迟，比 FASTEST 慢但足够用于计步
        )
    }

    @SuppressLint("ForegroundServiceType")
    private fun startForegroundService() {
        val channelId = "step_counter_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "计步器服务",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("计步器正在运行")
            .setContentText("实时记录您的跑步数据")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 使用系统图标
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    // 其余代码保持不变...
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                detectStep(it.values[0], it.values[1], it.values[2])
            }
        }
    }

    private fun detectStep(x: Float, y: Float, z: Float) {
        if (!_stepData.value.isRunning) return

        val acceleration = sqrt(
            (x * x + y * y + z * z).toDouble()
        )

        if (acceleration > ACCELERATION_THRESHOLD) {
            val currentTime = System.currentTimeMillis()

            // 防止连续计数，设置最小间隔200ms
            if (currentTime - lastStepTime > 200) {
                stepCount++
                lastStepTime = currentTime
                stepTimes.add(currentTime)

                // 清理超过1分钟的步数记录
                while (stepTimes.isNotEmpty() && stepTimes.first < currentTime - 60000) {
                    stepTimes.removeFirst()
                }

                updateStepData()
                checkPaceAlert()
            }
        }
    }

    private fun updateStepData() {
        val currentData = _stepData.value

        // 计算当前步频（基于最近1分钟的步数）
        val currentPace = stepTimes.size

        // 计算距离和卡路里
        val distance = stepCount * 0.7 / 1000.0
        val calories = stepCount * 0.04

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

        // 当当前步频接近目标步频时给出提示
        if (currentPace in (targetPace - 5)..(targetPace + 5)) {
            // 震动提示
            if (vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }
        }
    }

    fun updateTargetPace(newPace: Int) {
        _stepData.value = _stepData.value.copy(targetPace = newPace)
    }

    fun startRunning() {
        stepCount = 0
        stepTimes.clear()
        _stepData.value = StepCounterData(
            isRunning = true,
            startTime = System.currentTimeMillis()
        )
    }

    fun stopRunning() {
        _stepData.value = _stepData.value.copy(isRunning = false)
    }

    fun reset() {
        stepCount = 0
        stepTimes.clear()
        _stepData.value = StepCounterData()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}