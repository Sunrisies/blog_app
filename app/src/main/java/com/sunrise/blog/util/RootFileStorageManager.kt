package com.sunrise.blog.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * 根目录文件存储管理器
 * 根据Android版本自动选择最佳方法在根目录创建目录和文件
 */
class RootFileStorageManager(private val context: Context) {

    /**
     * 检查设备是否已Root
     */
    fun isDeviceRooted(): Boolean {
        return try {
            val paths = arrayOf(
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su"
            )
            paths.any { File(it).exists() }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 检查是否有MANAGE_EXTERNAL_STORAGE权限（Android 11+）
     */
    fun hasManageExternalStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * 检查是否可以访问根目录
     */
    fun canAccessRootDirectory(): Boolean {
        return when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> {
                // Android 9及以下：直接可以访问
                true
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+：需要MANAGE_EXTERNAL_STORAGE权限
                hasManageExternalStoragePermission()
            }
            else -> {
                // Android 10：需要特殊处理
                // 尝试创建测试文件来验证权限
                try {
                    val testFile = File(Environment.getExternalStorageDirectory(), ".permission_test")
                    val canCreate = testFile.createNewFile()
                    if (canCreate) testFile.delete()
                    canCreate
                } catch (e: Exception) {
                    false
                }
            }
        }
    }

    /**
     * 获取根目录路径
     */
    fun getRootPath(): String {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    /**
     * 根据Android版本自动选择最佳方法创建目录
     * 必须在根目录下创建，不接受其他位置
     */
    fun createRootDirectory(directoryPath: String): Result<String> {
        return when {
            // Android 9及以下：直接使用File API
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> {
                createDirectoryLegacy(directoryPath)
            }
            
            // Android 11+：如果有MANAGE_EXTERNAL_STORAGE权限，使用File API
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasManageExternalStoragePermission() -> {
                createDirectoryLegacy(directoryPath)
            }
            
            // Android 10：尝试多种方法
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                createDirectoryAndroid10(directoryPath)
            }
            
            // Android 11+ 无特殊权限：提示用户授予权限
            else -> {
                Result.failure(Exception("需要MANAGE_EXTERNAL_STORAGE权限，请在设置中授予"))
            }
        }
    }

    /**
     * 传统方法创建目录（适用于Android 9及以下，或有特殊权限的Android 11+）
     */
    private fun createDirectoryLegacy(directoryPath: String): Result<String> {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            
            // 创建目录
            val success = targetDir.mkdirs()
            
            if (success || targetDir.exists()) {
                Result.success(targetDir.absolutePath)
            } else {
                Result.failure(Exception("无法创建目录: ${targetDir.absolutePath}"))
            }
        } catch (e: SecurityException) {
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("创建失败: ${e.message}"))
        }
    }

    /**
     * Android 10专用方法 - 尝试多种策略
     */
    private fun createDirectoryAndroid10(directoryPath: String): Result<String> {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            
            // 策略1：直接尝试创建
            if (targetDir.mkdirs() || targetDir.exists()) {
                return Result.success(targetDir.absolutePath)
            }
            
            // 策略2：使用MediaStore创建文件，然后尝试移动（有限制）
            // 注意：Android 10+ 无法直接在根目录创建目录，这是系统限制
            
            Result.failure(
                Exception(
                    "Android 10系统限制无法在根目录创建目录。\n" +
                    "解决方案：\n" +
                    "1. 升级到Android 11+并授予MANAGE_EXTERNAL_STORAGE权限\n" +
                    "2. 使用Download目录代替\n" +
                    "3. 获取root权限"
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("创建失败: ${e.message}"))
        }
    }

    /**
     * 使用Root权限创建目录（需要设备已root）
     */
    fun createDirectoryWithRoot(directoryPath: String): Result<String> {
        return try {
            if (!isDeviceRooted()) {
                return Result.failure(Exception("设备未root"))
            }
            
            val fullPath = "${getRootPath()}/$directoryPath"
            val command = "mkdir -p \"$fullPath\""
            
            // 执行root命令
            val process = Runtime.getRuntime().exec("su -c $command")
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                // 验证目录是否创建成功
                val targetDir = File(fullPath)
                if (targetDir.exists() && targetDir.isDirectory) {
                    Result.success(fullPath)
                } else {
                    Result.failure(Exception("Root命令执行成功但目录不存在"))
                }
            } else {
                Result.failure(Exception("Root命令执行失败，退出码: $exitCode"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Root创建失败: ${e.message}"))
        }
    }

    /**
     * 在根目录创建文件
     */
    fun createFileInRootDir(directoryPath: String, fileName: String, content: String): Result<String> {
        return try {
            // 先确保目录存在
            val directoryResult = createRootDirectory(directoryPath)
            if (directoryResult.isFailure) {
                return Result.failure(directoryResult.exceptionOrNull()!!)
            }
            
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            val file = File(targetDir, fileName)
            
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: SecurityException) {
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("创建文件失败: ${e.message}"))
        }
    }

    /**
     * 读取根目录下的文件
     */
    fun readFromRootDir(directoryPath: String, fileName: String): String? {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            val file = File(targetDir, fileName)
            
            if (file.exists() && file.isFile) {
                FileInputStream(file).use { inputStream ->
                    inputStream.readBytes().toString(Charsets.UTF_8)
                }
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 列出根目录下的文件
     */
    fun listFilesInRootDir(directoryPath: String): List<File> {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            
            if (targetDir.exists() && targetDir.isDirectory) {
                targetDir.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 删除根目录下的文件
     */
    fun deleteFromRootDir(directoryPath: String, fileName: String): Boolean {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            val file = File(targetDir, fileName)
            
            if (file.exists() && file.isFile) {
                file.delete()
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查根目录下的目录是否存在
     */
    fun isRootDirectoryExists(directoryPath: String): Boolean {
        return try {
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            targetDir.exists() && targetDir.isDirectory
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 获取当前Android版本的权限状态信息
     */
    fun getPermissionStatus(): String {
        return when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> {
                "Android ${Build.VERSION.RELEASE} - 可以直接访问根目录"
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                if (hasManageExternalStoragePermission()) {
                    "Android 10 - 有特殊权限，可以访问根目录"
                } else {
                    "Android 10 - 系统限制，无法在根目录创建目录"
                }
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (hasManageExternalStoragePermission()) {
                    "Android ${Build.VERSION.RELEASE} - 有MANAGE_EXTERNAL_STORAGE权限"
                } else {
                    "Android ${Build.VERSION.RELEASE} - 需要MANAGE_EXTERNAL_STORAGE权限"
                }
            }
            else -> "未知Android版本"
        }
    }

    /**
     * 获取建议的解决方案
     */
    fun getRecommendedSolution(): String {
        return when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> {
                "您的设备支持直接在根目录创建目录，无需特殊操作。"
            }
            Build.VERSION.SDK_INT == Build.VERSION_CODES.Q -> {
                "Android 10系统限制无法在根目录创建目录。\n" +
                "建议：\n" +
                "1. 升级到Android 11+\n" +
                "2. 使用Download目录代替\n" +
                "3. 如果设备已root，使用root权限"
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                if (hasManageExternalStoragePermission()) {
                    "您已有权限，可以直接在根目录创建目录。"
                } else {
                    "需要授予MANAGE_EXTERNAL_STORAGE权限：\n" +
                    "1. 点击下方按钮打开设置\n" +
                    "2. 授予所有文件访问权限\n" +
                    "3. 返回应用重试"
                }
            }
            else -> "未知情况，请联系开发者"
        }
    }

    /**
     * 打开权限设置页面（Android 11+）
     * 支持多种方式，兼容不同厂商系统
     */
    fun openPermissionIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // 尝试多种方式打开权限设置
            val intents = arrayOf(
                // 方式1：标准Android权限设置（可能在MIUI上不可用）
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                // 方式2：应用信息页面（MIUI兼容）
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                // 方式3：存储访问设置
                Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"),
                // 方式4：通用应用设置
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
            
            // 返回第一个可用的intent
            intents[0]
        } else {
            Intent(Settings.ACTION_APPLICATION_SETTINGS)
        }
    }

    /**
     * 检查指定intent是否可用
     */
    fun isIntentAvailable(intent: Intent): Boolean {
        return try {
            val resolveInfo = context.packageManager.resolveActivity(intent, 0)
            resolveInfo != null
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取可用的权限设置intent（MIUI兼容版本）
     */
    fun getAvailablePermissionIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intents = arrayOf(
                // 1. 尝试标准Android权限设置
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                // 2. 尝试MIUI特定的权限设置
                Intent("android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"),
                // 3. 应用信息页面（最兼容）
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${context.packageName}")
                },
                // 4. 通用应用设置
                Intent(Settings.ACTION_APPLICATION_SETTINGS)
            )
            
            // 逐个检查，返回第一个可用的
            for (intent in intents) {
                if (isIntentAvailable(intent)) {
                    return intent
                }
            }
        }
        
        // Android 11以下或所有intent都不可用时，返回应用信息页面
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }

    /**
     * 获取权限设置的详细说明（根据MIUI特性）
     */
    fun getPermissionGuide(): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                "MIUI系统权限设置指南：\n" +
                "1. 点击'打开设置'按钮\n" +
                "2. 如果自动打开失败，请手动操作：\n" +
                "   - 打开手机设置\n" +
                "   - 找到'应用管理'或'应用'\n" +
                "   - 选择'博客'应用\n" +
                "   - 点击'权限'\n" +
                "   - 找到'存储'或'所有文件访问权限'\n" +
                "   - 启用权限\n" +
                "3. 返回应用重试\n\n" +
                "注意：部分MIUI版本可能需要在'安全中心'中设置"
            }
            else -> {
                "Android 9及以下版本无需特殊权限设置"
            }
        }
    }
}
