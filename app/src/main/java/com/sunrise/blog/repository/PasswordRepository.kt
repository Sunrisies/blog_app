// PasswordRepository.kt
package com.sunrise.blog.repository

import com.sunrise.blog.database.PasswordDatabase
import com.sunrise.blog.database.PasswordEntity
import com.sunrise.blog.model.PasswordItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PasswordRepository(private val database: PasswordDatabase) {

    fun getAllPasswords(): Flow<List<PasswordItem>> {
        return database.passwordDao().getAll().map { entities ->
            entities.map { it.toModel() }
        }
    }

    suspend fun getPasswordById(id: String): PasswordItem? {
        return database.passwordDao().getById(id)?.toModel()
    }

    suspend fun insertPassword(password: PasswordItem) {
        database.passwordDao().insert(PasswordEntity.fromModel(password))
    }

    suspend fun updatePassword(password: PasswordItem) {
        database.passwordDao().update(PasswordEntity.fromModel(password))
    }

    suspend fun deletePassword(password: PasswordItem) {
        database.passwordDao().delete(PasswordEntity.fromModel(password))
    }

    suspend fun getPasswordCount(): Int {
        return database.passwordDao().getCount()
    }

    suspend fun getCategories(): List<String> {
        return database.passwordDao().getCategories()
    }
}