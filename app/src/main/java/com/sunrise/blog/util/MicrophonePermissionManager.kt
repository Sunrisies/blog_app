package com.sunrise.blog.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * 麦克风权限管理器
 * 提供检查和请求麦克风权限的功能
 */
class MicrophonePermissionManager(private val context: Context) {

    /**
     * 检查是否已获得麦克风权限
     * @return 是否已授权
     */
    fun isMicrophonePermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 获取需要请求的权限列表
     * @return 权限数组
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) 及以上版本
            arrayOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            // Android 13 以下版本
            arrayOf(Manifest.permission.RECORD_AUDIO)
        }
    }

    /**
     * 检查是否需要向用户解释权限请求
     * @param permissions 被拒绝的权限列表
     * @return 是否需要解释
     */
    fun shouldShowRequestPermissionRationale(permissions: Array<String>): Boolean {
        // 这个方法通常在Activity/Fragment中调用，这里只是提供接口
        return false
    }
}