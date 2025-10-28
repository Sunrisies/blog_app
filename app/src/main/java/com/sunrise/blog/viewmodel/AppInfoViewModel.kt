// AppInfoViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.sunrise.blog.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppInfoViewModel(application: Application) : AndroidViewModel(application) {
    private val _appInfo = MutableStateFlow(AppInfo())
    val appInfo: StateFlow<AppInfo> = _appInfo.asStateFlow()

    companion object {
        private const val TAG = "AppInfoViewModel"
        private const val DEVELOPER_NAME = "Sunrise Team"
        private const val DEVELOPER_EMAIL = "support@sunrise.com"
        private const val APP_NAME = "步频器专业版"
    }

    init {
        loadAppInfo()
    }

    private fun loadAppInfo() {
        try {
            val context = getApplication<Application>()
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0)
            }

            val appInfo = packageInfo.versionName?.let {
                AppInfo(
                    appName = getApplicationName(packageManager, packageName),
                    versionName = it,
                    versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        packageInfo.longVersionCode
                    } else {
                        @Suppress("DEPRECATION")
                        packageInfo.versionCode.toLong()
                    },
                    packageName = packageName,
                    buildDate = getBuildDate(packageInfo),
                    developer = DEVELOPER_NAME,
                    email = DEVELOPER_EMAIL
                )
            }

            if (appInfo != null) {
                _appInfo.value = appInfo
                Log.d(TAG, "应用信息加载完成: ${appInfo.appName} v${appInfo.versionName} (${appInfo.versionCode})")
            }

        } catch (e: Exception) {
            Log.e(TAG, "加载应用信息失败", e)
            // 提供默认值
            _appInfo.value = AppInfo(
                appName = APP_NAME,
                versionName = "1.0.0",
                versionCode = 1,
                packageName = "com.sunrise.blog",
                buildDate = "未知",
                developer = DEVELOPER_NAME,
                email = DEVELOPER_EMAIL
            )
        }
    }

    private fun getApplicationName(packageManager: PackageManager, packageName: String): String {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(applicationInfo).toString()
        } catch (e: Exception) {
            APP_NAME
        }
    }

    private fun getBuildDate(packageInfo: PackageInfo): String {
        return try {
            val apkPath = getApplication<Application>().packageManager
                .getApplicationInfo(getApplication<Application>().packageName, 0).sourceDir
            val apkFile = java.io.File(apkPath)
            val lastModified = Date(apkFile.lastModified())
            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(lastModified)
        } catch (e: Exception) {
            "未知"
        }
    }

    // 获取详细的应用信息字符串（用于显示）
    fun getAppInfoDetail(): String {
        val info = _appInfo.value
        return """
            |应用名称: ${info.appName}
            |版本号: ${info.versionName}
            |内部版本: ${info.versionCode}
            |开发者: ${info.developer}
            |联系邮箱: ${info.email}
            |构建时间: ${info.buildDate}
            |包名: ${info.packageName}
        """.trimMargin()
    }

    // 检查更新（这里可以扩展为真正的更新检查）
    fun checkForUpdates(): Map<String, Any> {
        return mapOf(
            "hasUpdate" to false,
            "latestVersion" to _appInfo.value.versionName,
            "updateUrl" to "",
            "releaseNotes" to "当前已是最新版本"
        )
    }
}