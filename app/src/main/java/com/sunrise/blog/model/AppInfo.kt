package com.sunrise.blog.model

data class AppInfo(
    val appName: String = "",
    val versionName: String = "",
    val versionCode: Long = 0,
    val packageName: String = "",
    val buildDate: String = "",
    val developer: String = "Sunrise Team",
    val email: String = "support@sunrise.com"
)