// StepCounterViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.sunrise.blog.model.StepCounterData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepCounterViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {
    private val _stepData = MutableStateFlow(StepCounterData())
    val stepData: StateFlow<StepCounterData> = _stepData.asStateFlow()

    private lateinit var sensorManager: SensorManager
    private lateinit var stepSensor: Sensor

    // 简单的步数检测变量
    private var stepCount = 0
    private var lastStepTime = 0L

    init {
        initSensors()
    }

    private fun initSensors() {
        sensorManager = getApplication<Application>().getSystemService(SensorManager::class.java)
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!! // 使用加速度传感器

        // 注册传感器监听
        sensorManager.registerListener(
            this,
            stepSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            detectStep(event.values[0], event.values[1], event.values[2])
        }
    }

    private fun detectStep(x: Float, y: Float, z: Float) {
        if (!_stepData.value.isRunning) return

        val acceleration = Math.sqrt(
            (x * x + y * y + z * z).toDouble()
        )

        // 简单的步数检测逻辑
        if (acceleration > 12.0) { // 阈值可以根据需要调整
            val currentTime = System.currentTimeMillis()

            // 防止连续计数，设置最小间隔
            if (currentTime - lastStepTime > 300) {
                stepCount++
                lastStepTime = currentTime

                updateStepData()
            }
        }
    }

    private fun updateStepData() {
        val currentData = _stepData.value
        // 简单计算：每步约0.7米，每步约0.04卡路里
        val distance = stepCount * 0.7 / 1000.0 // 转换为公里
        val calories = stepCount * 0.04

        _stepData.value = currentData.copy(
            steps = stepCount,
            distance = distance,
            calories = calories
        )
    }

    fun startRunning() {
        stepCount = 0
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
        _stepData.value = StepCounterData()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // 不需要处理
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(this)
    }
}