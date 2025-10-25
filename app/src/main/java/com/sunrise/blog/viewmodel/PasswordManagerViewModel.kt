// PasswordManagerViewModel.kt
package com.sunrise.blog.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.sunrise.blog.database.PasswordDatabase
import com.sunrise.blog.model.ImportData
import com.sunrise.blog.model.PasswordItem
import com.sunrise.blog.repository.PasswordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class PasswordManagerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PasswordRepository
    private val _passwords = mutableStateListOf<PasswordItem>()
    val passwords: List<PasswordItem> get() = _passwords

    private val _selectedPassword = mutableStateOf<PasswordItem?>(null)
    val selectedPassword: PasswordItem? get() = _selectedPassword.value

    private val _showDeleteDialog = mutableStateOf(false)
    val showDeleteDialog: Boolean get() = _showDeleteDialog.value

    private val _showImportDialog = mutableStateOf(false)
    val showImportDialog: Boolean get() = _showImportDialog.value

    private val _showEditDialog = mutableStateOf(false)
    val showEditDialog: Boolean get() = _showEditDialog.value

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _importResult = MutableStateFlow<ImportResult?>(null)
    val importResult: StateFlow<ImportResult?> = _importResult.asStateFlow()

    init {
        val database = PasswordDatabase.getInstance(application)
        repository = PasswordRepository(database)
        loadPasswords()
    }

    private fun loadPasswords() {
        viewModelScope.launch {
            repository.getAllPasswords().collect { passwordList ->
                _passwords.clear()
                _passwords.addAll(passwordList)
                _isLoading.value = false
            }
        }
    }

    fun addPassword(password: PasswordItem) {
        viewModelScope.launch {
            val newPassword = password.copy(
                id = System.currentTimeMillis().toString(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertPassword(newPassword)
            // 数据会自动通过 Flow 更新
        }
    }

    fun updatePassword(password: PasswordItem) {
        viewModelScope.launch {
            repository.updatePassword(password.copy(updatedAt = System.currentTimeMillis()))
            // 数据会自动通过 Flow 更新
        }
    }

    fun deletePassword(password: PasswordItem) {
        viewModelScope.launch {
            repository.deletePassword(password)
            _showDeleteDialog.value = false
            _selectedPassword.value = null
        }
    }

    fun selectPassword(password: PasswordItem?) {
        _selectedPassword.value = password
    }

    fun showDeleteDialog(show: Boolean) {
        _showDeleteDialog.value = show
    }

    fun showImportDialog(show: Boolean) {
        _showImportDialog.value = show
    }

    fun showAddDialog() {
        _selectedPassword.value = null
        _showEditDialog.value = true
    }

    fun closeEditDialog() {
        _showEditDialog.value = false
        _selectedPassword.value = null
    }

    fun importFromJson(json: String): ImportResult {
        return try {
            val gson = Gson()
            val importData = gson.fromJson(json, ImportData::class.java)

            var updatedCount = 0
            var addedCount = 0

            viewModelScope.launch {
                importData.passwords.forEach { importedPassword ->
                    val existingPassword = _passwords.find { it.id == importedPassword.id }
                    if (existingPassword != null) {
                        // 更新现有项
                        repository.updatePassword(importedPassword.copy(updatedAt = System.currentTimeMillis()))
                        updatedCount++
                    } else {
                        // 添加新项
                        val newPassword = importedPassword.copy(
                            id = System.currentTimeMillis().toString(),
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        repository.insertPassword(newPassword)
                        addedCount++
                    }
                }
            }

            val result = ImportResult.Success(addedCount, updatedCount)
            _importResult.value = result
            result
        } catch (e: Exception) {
            val result = ImportResult.Error(e.message ?: "导入失败")
            _importResult.value = result
            result
        }
    }

    fun exportToJson(): String {
        val exportData = ImportData(
            passwords = _passwords,
            exportedAt = System.currentTimeMillis()
        )
        return Gson().toJson(exportData)
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}

sealed class ImportResult {
    data class Success(val added: Int, val updated: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}