package com.sunrise.blog.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings

/**
 * 权限测试管理器
 * 提供关闭和重新打开权限的功能，用于测试权限处理逻辑
 */
class PermissionTestManager(private val context: Context) {

    /**
     * 检查是否有MANAGE_EXTERNAL_STORAGE权限
     */
    fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * 获取当前权限状态描述
     */
    fun getPermissionStatus(): String {
        return if (hasManageExternalStoragePermission()) {
            "当前状态：已授予所有文件访问权限"
        } else {
            "当前状态：未授予所有文件访问权限"
        }
    }

    /**
     * 打开系统设置页面（用于手动关闭/打开权限）
     * 这会打开应用信息页面，用户可以手动管理权限
     */
    fun openAppSettings(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    /**
     * 打开所有文件访问权限设置页面（如果可用）
     */
    fun openAllFilesAccessSettings(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } else {
            openAppSettings()
        }
    }

    /**
     * 打开权限管理指南
     * 提供详细的手动操作步骤
     */
    fun getPermissionManagementGuide(): String {
        return """
            🔧 手动关闭/重新打开权限步骤：
            
            方法1：通过应用信息（推荐）
            1. 点击下方"打开应用设置"按钮
            2. 找到"权限"或"Permissions"
            3. 找到"存储"或"All files access"
            4. 选择"拒绝"或"Deny"来关闭权限
            5. 再次选择"允许"或"Allow"来重新打开权限
            
            方法2：通过系统设置
            1. 打开手机设置
            2. 进入"应用管理"或"Apps"
            3. 找到"博客"应用
            4. 点击"权限"
            5. 关闭再打开存储权限
            
            方法3：通过安全中心（MIUI）
            1. 打开"安全中心"
            2. 进入"权限管理"
            3. 找到"博客"应用
            4. 管理存储权限
            
            💡 提示：
            - 关闭权限后，应用将无法访问根目录
            - 重新打开权限后，需要返回应用重新检测
            - 可以观察应用的权限状态变化
        """.trimIndent()
    }

    /**
     * 检查权限是否被拒绝
     */
    fun isPermissionDenied(): Boolean {
        return !hasManageExternalStoragePermission()
    }

    /**
     * 获取权限操作建议
     */
    fun getActionSuggestion(): String {
        return when {
            hasManageExternalStoragePermission() -> {
                "当前有权限，您可以尝试：\n" +
                "1. 点击'打开应用设置'手动关闭权限\n" +
                "2. 返回应用观察权限状态变化\n" +
                "3. 再次打开权限测试应用响应"
            }
            else -> {
                "当前无权限，您可以尝试：\n" +
                "1. 点击'打开所有文件权限设置'重新授权\n" +
                "2. 或使用'打开应用设置'手动管理\n" +
                "3. 授权后返回应用测试功能"
            }
        }
    }

    /**
     * 创建测试目录（需要权限）
     */
    fun createTestDirectory(directoryPath: String): Result<String> {
        return try {
            if (!hasManageExternalStoragePermission()) {
                return Result.failure(Exception("当前无权限，无法创建目录"))
            }

            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = java.io.File(rootDir, directoryPath)
            
            if (targetDir.mkdirs() || targetDir.exists()) {
                Result.success("测试目录创建成功：${targetDir.absolutePath}")
            } else {
                Result.failure(Exception("创建目录失败"))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("权限不足：${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("创建失败：${e.message}"))
        }
    }

    /**
     * 检查并返回权限相关的所有信息
     */
    fun getPermissionInfo(): String {
        val status = getPermissionStatus()
        val hasPermission = hasManageExternalStoragePermission()
        val androidVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        
        return """
            📱 权限信息详情
            
            系统版本：$androidVersion
            $status
            权限状态：${if (hasPermission) "✅ 已授权" else "❌ 未授权"}
            
            当前可执行的操作：
            ${if (hasPermission) "✅ 可以创建根目录文件和目录" else "❌ 无法在根目录创建文件"}
            
            建议操作：
            ${getActionSuggestion()}
        """.trimIndent()
    }
}
