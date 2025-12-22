package com.sunrise.blog.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

/**
 * 文件权限管理器
 * 提供检查和请求文件存储权限的功能
 */
class FilePermissionManager(private val context: Context) {

    /**
     * 检查是否已获得文件存储权限
     * @return 是否已授权
     */
    fun isFilePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) 及以上版本使用新的媒体权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Android 13 以下版本使用传统存储权限
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 获取需要请求的权限列表
     * @return 权限数组
     */
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13 (API 33) 及以上版本
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            // Android 13 以下版本
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    /**
     * 检查是否需要向用户解释权限请求
     * @return 是否需要解释
     */
    fun shouldShowRequestPermissionRationale(): Boolean {
        // 这个方法通常在Activity/Fragment中调用，这里只是提供接口
        return false
    }

    /**
     * 获取权限状态描述
     * @return 权限状态字符串
     */
    fun getPermissionStatusDescription(): String {
        return if (isFilePermissionGranted()) {
            "文件权限已授予，可以正常访问存储"
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "文件权限未授予，需要READ_MEDIA_IMAGES、READ_MEDIA_VIDEO、READ_MEDIA_AUDIO权限"
            } else {
                "文件权限未授予，需要WRITE_EXTERNAL_STORAGE和READ_EXTERNAL_STORAGE权限"
            }
        }
    }
}
