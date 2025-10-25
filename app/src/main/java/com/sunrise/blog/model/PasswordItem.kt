// PasswordItem.kt
package com.sunrise.blog.model

import androidx.compose.runtime.Immutable

@Immutable
data class PasswordItem(
    val id: String = "",
    val name: String = "",
    val account: String = "",
    val password: String = "",
    val website: String = "",
    val notes: String = "",
    val category: String = "默认",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// 导入的数据结构
data class ImportData(
    val passwords: List<PasswordItem> = emptyList(),
    val version: String = "1.0",
    val exportedAt: Long = System.currentTimeMillis()
)