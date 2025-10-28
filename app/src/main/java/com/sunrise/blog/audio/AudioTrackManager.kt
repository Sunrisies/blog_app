// AudioTrackManager.kt
package com.sunrise.blog.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlin.math.PI
import kotlin.math.sin

class AudioTrackManager {
    private var audioTrack: AudioTrack? = null
    private var beepData: ByteArray? = null

    companion object {
        private const val TAG = "AudioTrackManager"
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BEEP_DURATION_MS = 80 // 滴答声持续时间（毫秒）
    }

    /**
     * 初始化 AudioTrack
     */
    fun initialize() {
        cleanup() // 先清理之前的资源

        try {
            // 生成滴答声数据
            beepData = generateBeepSound(0.0, BEEP_DURATION_MS) // 1000Hz 频率

            // 创建静态模式的 AudioTrack
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                beepData!!.size,
                AudioTrack.MODE_STATIC
            )

            // 写入音频数据
            val written = audioTrack!!.write(beepData!!, 0, beepData!!.size)
            if (written != beepData!!.size) {
                Log.e(TAG, "Failed to write complete audio data: $written vs ${beepData!!.size}")
                cleanup()
                return
            }

            Log.d(TAG, "AudioTrack initialized successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error initializing AudioTrack: ${e.message}")
            cleanup()
        }
    }

    /**
     * 生成滴答声
     */
    private fun generateBeepSound(frequency: Double, durationMs: Int): ByteArray {
        val SAMPLE_RATE = 44100
        val tickFreq  = 800.0          // 滴答主频（可调）
        val tockFreq  = 600.0          // 滴～答略低
        val tickDur   = 60             // 单声长度 ms（越短越脆）
        val tockDelay = 120            // 答比滴晚出来多少 ms（节奏间隔）
        val cycles    = durationMs / (tockDelay + 60) // 完整滴答组数

        val totalSamples = (SAMPLE_RATE * durationMs) / 1000
        val buffer = ByteArray(totalSamples * 2)

        var idx = 0
        while (idx < totalSamples) {
            val groupStart = idx
            // ===== 滴（tick） =====
            val tickSamples = (SAMPLE_RATE * tickDur) / 1000
            for (i in 0 until tickSamples) {
                val env = 1.0 - i.toDouble() / tickSamples // 快速衰减
                val sample = if (sin(2 * PI * i * tickFreq / SAMPLE_RATE) > 0) 0.7 else -0.7
                val out = (sample * env * Short.MAX_VALUE).toInt()
                buffer[idx * 2] = (out and 0xFF).toByte()
                buffer[idx * 2 + 1] = ((out ushr 8) and 0xFF).toByte()
                idx++
                if (idx >= totalSamples) return buffer
            }

            // ===== 答前静默 =====
            val silentSamples = (SAMPLE_RATE * (tockDelay - tickDur)) / 1000
            for (i in 0 until silentSamples) {
                buffer[idx * 2] = 0
                buffer[idx * 2 + 1] = 0
                idx++
                if (idx >= totalSamples) return buffer
            }

            // ===== 答（tock） =====
            val tockSamples = (SAMPLE_RATE * tickDur) / 1000
            for (i in 0 until tockSamples) {
                val env = 1.0 - i.toDouble() / tockSamples
                val sample = if (sin(2 * PI * i * tockFreq / SAMPLE_RATE) > 0) 0.7 else -0.7
                val out = (sample * env * Short.MAX_VALUE).toInt()
                buffer[idx * 2] = (out and 0xFF).toByte()
                buffer[idx * 2 + 1] = ((out ushr 8) and 0xFF).toByte()
                idx++
                if (idx >= totalSamples) return buffer
            }
        }
        return buffer
    }
//    private fun generateBeepSound(frequency: Double, durationMs: Int): ByteArray {
//        val numSamples = (SAMPLE_RATE * durationMs / 1000).toInt()
//        val buffer = ByteArray(numSamples * 2) // 16-bit = 2 bytes per sample
//
//        // 生成带包络的正弦波，使声音更自然
//        for (i in 0 until numSamples) {
//            // 应用 ADSR 包络：快速起音，缓慢释音
//            val envelope = when {
//                i < numSamples / 10 -> i.toDouble() / (numSamples / 10) // 起音
//                i > numSamples * 8 / 10 -> 1.0 - (i - numSamples * 8 / 10).toDouble() / (numSamples / 10) // 释音
//                else -> 1.0 // 持续
//            }
//
//            val sample = sin(2.0 * PI * i * frequency / SAMPLE_RATE)
//            val scaledSample = (sample * envelope * 0.7 * Short.MAX_VALUE).toInt()
//
//            // 转换为16-bit little-endian
//            buffer[i * 2] = (scaledSample and 0xFF).toByte()
//            buffer[i * 2 + 1] = ((scaledSample ushr 8) and 0xFF).toByte()
//        }
//
//        return buffer
//    }

    /**
     * 播放滴答声
     */
    fun playTick() {
        try {
            audioTrack?.let { track ->
                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    // 如果正在播放，先停止
                    if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                        track.stop()
                    }

                    // 重新加载静态数据并播放
                    track.reloadStaticData()
                    track.play()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error playing tick sound: ${e.message}")
        }
    }

    /**
     * 设置音量
     */
    fun setVolume(volume: Int) {
        try {
            val normalizedVolume = (volume / 100.0f).coerceIn(0.0f, 1.0f)
            audioTrack?.setVolume(normalizedVolume)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting volume: ${e.message}")
        }
    }

    /**
     * 清理资源
     */
    fun cleanup() {
        try {
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up AudioTrack: ${e.message}")
        }

        audioTrack = null
        beepData = null
    }

    /**
     * 检查是否已初始化
     */
    fun isInitialized(): Boolean {
        return audioTrack?.state == AudioTrack.STATE_INITIALIZED
    }
}