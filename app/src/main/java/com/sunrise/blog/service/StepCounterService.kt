//// StepCounterService.kt
//package com.sunrise.blog.service
//
//import android.app.*
//import android.content.Context
//import android.content.Intent
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.os.Binder
//import android.os.IBinder
//import android.os.PowerManager
//import androidx.core.app.NotificationCompat
//import com.sunrise.blog.MainActivity
//import com.sunrise.blog.R
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlin.math.sqrt
//import kotlin.math.pow
//
//class StepCounterService : Service(), SensorEventListener {
//    private val binder = StepCounterBinder()
//    private lateinit var sensorManager: SensorManager
//    private lateinit var accelerometer: Sensor
//    private lateinit var powerManager: PowerManager
//    private lateinit var wakeLock: PowerManager.WakeLock
//
//    private val _stepData = MutableStateFlow(StepCounterData())
//    val stepData: StateFlow<StepCounterData> = _stepData
//
//    private var stepCount = 0
//    private var lastStepTime = 0L
//    private val stepBuffer = mutableListOf<Long>()
//    private val ACCELERATION_THRESHOLD = 12.0
//
//    inner class StepCounterBinder : Binder() {
//        fun getService(): StepCounterService = this@StepCounterService
//    }
//
//    override fun onBind(intent: Intent): IBinder = binder
//
//    override fun onCreate() {
//        super.onCreate()
//        initializeSensors()
//        startForegroundService()
//    }
//
//    private fun initializeSensors() {
//        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
//        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
//        powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "StepCounter:WakeLock")
//        wakeLock.acquire()
//    }
//
//    private fun startForegroundService() {
//        val channelId = "step_counter_channel"
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(
//                channelId,
//                "计步器服务",
//                NotificationManager.IMPORTANCE_LOW
//            )
//            val manager = getSystemService(NotificationManager::class.java)
//            manager.createNotificationChannel(channel)
//        }
//
//        val intent = Intent(this, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
//        }
//        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setContentTitle("计步器正在运行")
//            .setContentText("实时记录您的运动数据")
//            .setSmallIcon(R.drawable.ic_walk)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        startForeground(1, notification)
//    }
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
//        return START_STICKY
//    }
//
//    override fun onSensorChanged(event: SensorEvent?) {
//        event?.let {
//            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
//                detectStep(it.values[0], it.values[1], it.values[2])
//            }
//        }
//    }
//
//    private fun detectStep(x: Float, y: Float, z: Float) {
//        val acceleration = sqrt(x.toDouble().pow(2) + y.toDouble().pow(2) + z.toDouble().pow(2))
//        val currentTime = System.currentTimeMillis()
//
//        if (acceleration > ACCELERATION_THRESHOLD) {
//            // 防止连续计数，设置最小间隔
//            if (currentTime - lastStepTime > 200) {
//                stepCount++
//                lastStepTime = currentTime
//                stepBuffer.add(currentTime)
//
//                // 清理超过1分钟的步数记录
//                stepBuffer.removeAll { it < currentTime - 60000 }
//
//                updateStepData()
//            }
//        }
//    }
//
//    private fun updateStepData() {
//        val currentData = _stepData.value
//        val currentTime = System.currentTimeMillis()
//
//        // 计算实时步频（步/分钟）
//        val stepFrequency = stepBuffer.size * 60.0 / 60 // 基于最近1分钟的步数
//
//        // 计算配速（分钟/公里），假设平均步长0.7米
//        val pace = if (stepFrequency > 0) 1000.0 / (stepFrequency * 0.7) else 0.0
//
//        // 计算卡路里（MET方法估算）
//        val calories = stepCount * 0.04
//
//        // 计算距离（公里）
//        val distance = stepCount * 0.7 / 1000.0
//
//        _stepData.value = currentData.copy(
//            totalSteps = stepCount,
//            currentSessionSteps = stepCount,
//            calories = calories,
//            distance = distance,
//            pace = pace,
//            lastStepTime = lastStepTime
//        )
//    }
//
//    fun startSession() {
//        stepCount = 0
//        stepBuffer.clear()
//        _stepData.value = StepCounterData(
//            isRunning = true,
//            startTime = System.currentTimeMillis()
//        )
//    }
//
//    fun stopSession() {
//        _stepData.value = _stepData.value.copy(isRunning = false)
//    }
//
//    fun resetSession() {
//        stepCount = 0
//        stepBuffer.clear()
//        _stepData.value = StepCounterData()
//    }
//
//    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//
//    override fun onDestroy() {
//        super.onDestroy()
//        sensorManager.unregisterListener(this)
//        wakeLock.release()
//    }
//}