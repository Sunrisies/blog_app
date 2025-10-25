// PasswordEntity.kt
package com.sunrise.blog.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "passwords")
data class PasswordEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val account: String,
    val password: String,
    val website: String,
    val notes: String,
    val category: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        fun fromModel(model: com.sunrise.blog.model.PasswordItem): PasswordEntity {
            return PasswordEntity(
                id = model.id,
                name = model.name,
                account = model.account,
                password = model.password,
                website = model.website,
                notes = model.notes,
                category = model.category,
                createdAt = model.createdAt,
                updatedAt = model.updatedAt
            )
        }
    }

    fun toModel(): com.sunrise.blog.model.PasswordItem {
        return com.sunrise.blog.model.PasswordItem(
            id = id,
            name = name,
            account = account,
            password = password,
            website = website,
            notes = notes,
            category = category,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}