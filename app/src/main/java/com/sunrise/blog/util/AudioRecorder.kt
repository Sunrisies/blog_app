package com.sunrise.blog.util

import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException

/**
 * 音频录制管理类
 */
class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: String? = null
    private var isRecording = false

    /**
     * 开始录音
     * @param outputFilePath 输出文件路径
     * @return 是否成功开始录音
     */
    fun startRecording(outputFilePath: String): Boolean {
        return try {
            // 确保输出目录存在
            val file = File(outputFilePath)
            val parentDir = file.parentFile
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs()
            }

            outputFile = outputFilePath
            
            if (mediaRecorder == null) {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder()
                } else {
                    @Suppress("DEPRECATION")
                    MediaRecorder()
                }
            }

            // 配置MediaRecorder
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile)
                
                prepare()
                start()
                isRecording = true
            }
            
            true
        } catch (e: IOException) {
            e.printStackTrace()
            stopRecording()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            stopRecording()
            false
        }
    }

    /**
     * 停止录音
     * @return 录音文件路径，如果无录音则返回null
     */
    fun stopRecording(): String? {
        try {
            if (isRecording && mediaRecorder != null) {
                mediaRecorder?.apply {
                    stop()
                    reset()
                    release()
                }
                isRecording = false
                mediaRecorder = null
                
                return outputFile
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return null
    }

    /**
     * 检查是否正在录音
     * @return 是否正在录音
     */
    fun isRecording(): Boolean {
        return isRecording
    }
}