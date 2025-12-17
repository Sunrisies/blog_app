package com.sunrise.blog.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

/**
 * 文件存储管理器
 * 提供创建目录、创建文件和读取文件的功能
 */
class FileStorageManager(private val context: Context) {

    /**
     * 获取应用内部存储根目录
     */
    fun getInternalStorageRoot(): File {
        return context.filesDir
    }

    /**
     * 获取外部存储根目录
     */
    fun getExternalStorageRoot(): File? {
        return Environment.getExternalStorageDirectory()
    }

    /**
     * 获取外部存储公共目录（如Documents目录）
     */
    fun getExternalDocumentsDir(): File? {
        return context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
    }

    /**
     * 列出根目录下的所有文件和目录
     */
    fun listRootFiles(): List<File> {
        val rootDir = getInternalStorageRoot()
        return rootDir.listFiles()?.toList() ?: emptyList()
    }

    /**
     * 创建目录在应用内部存储中
     * @param directoryName 目录名称
     * @return 创建的目录File对象，如果失败则返回null
     */
    fun createDirectory(directoryName: String): File? {
        return try {
            val directory = File(getInternalStorageRoot(), directoryName)
            if (!directory.exists()) {
                val success = directory.mkdirs()
                if (success) {
                    directory
                } else {
                    null
                }
            } else {
                directory
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 在外部存储根目录下创建目录
     * @param directoryName 目录名称
     * @return 创建的目录路径，如果失败则返回null
     */
    fun createDirectoryInExternalRoot(directoryName: String): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val externalRoot = Environment.getExternalStorageDirectory()
            val directory = File(externalRoot, directoryName)
            val mkdirsSuccess = directory.mkdirs()
            
            if (mkdirsSuccess) {
                Result.success(directory.absolutePath)
            } else {
                Result.failure(Exception("无法创建目录: ${directory.absolutePath}"))
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 在外部存储公共目录下创建目录
     * @param directoryName 目录名称
     * @param parentDirectoryType 父目录类型，例如 Environment.DIRECTORY_DOWNLOADS
     * @return 创建的目录路径，如果失败则返回null
     */
    fun createDirectoryInExternalPublicDir(directoryName: String, parentDirectoryType: String = Environment.DIRECTORY_DOWNLOADS): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val externalDir = Environment.getExternalStoragePublicDirectory(parentDirectoryType)
            if (!externalDir.exists()) {
                val mkdirsSuccess = externalDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建父目录: ${externalDir.absolutePath}"))
                }
            }
            
            val directory = File(externalDir, directoryName)
            val mkdirsSuccess = directory.mkdirs()
            
            if (mkdirsSuccess) {
                Result.success(directory.absolutePath)
            } else {
                Result.failure(Exception("无法创建目录: ${directory.absolutePath}"))
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 在应用内部存储根目录下创建文件
     * @param fileName 文件名
     * @param content 文件内容（可选）
     * @return 创建的文件File对象，如果失败则返回null
     */
    fun createFileInRoot(fileName: String, content: String? = null): File? {
        return try {
            val file = File(getInternalStorageRoot(), fileName)
            if (!file.exists()) {
                val parentDir = file.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                }
                val success = file.createNewFile()
                if (!success) {
                    return null
                }
            }
            
            // 如果提供了内容，则写入文件
            content?.let {
                val writeSuccess = writeFile(file, it)
                if (!writeSuccess) {
                    return null
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 在外部存储根目录下创建文件
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInExternalRoot(fileName: String, content: String): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val externalRoot = Environment.getExternalStorageDirectory()
            val file = File(externalRoot, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 在外部存储公共目录下创建文件（如Download、Documents等）
     * @param fileName 文件名
     * @param content 文件内容
     * @param directoryType 目录类型，例如 Environment.DIRECTORY_DOWNLOADS
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInExternalPublicDir(fileName: String, content: String, directoryType: String = Environment.DIRECTORY_DOWNLOADS): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val externalDir = Environment.getExternalStoragePublicDirectory(directoryType)
            if (!externalDir.exists()) {
                val mkdirsSuccess = externalDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建目录: ${externalDir.absolutePath}"))
                }
            }
            
            val file = File(externalDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            
            Result.success(file.absolutePath)
        } catch (e: SecurityException) {
            e.printStackTrace()
            Result.failure(Exception("权限不足: ${e.message}"))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 在指定目录下创建文件
     * @param directory 目录
     * @param fileName 文件名
     * @param content 文件内容（可选）
     * @return 创建的文件File对象，如果失败则返回null
     */
    fun createFile(directory: File, fileName: String, content: String? = null): File? {
        return try {
            val file = File(directory, fileName)
            if (!file.exists()) {
                val parentDir = file.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                }
                val success = file.createNewFile()
                if (!success) {
                    return null
                }
            }
            
            // 如果提供了内容，则写入文件
            content?.let {
                val writeSuccess = writeFile(file, it)
                if (!writeSuccess) {
                    return null
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 写入文件内容
     * @param file 目标文件
     * @param content 要写入的内容
     * @return 是否成功写入
     */
    fun writeFile(file: File, content: String): Boolean {
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 读取文件内容
     * @param file 要读取的文件
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFile(file: File): String? {
        return try {
            FileInputStream(file).use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 通过Uri读取文件内容
     * @param uri 文件Uri
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFileFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes().toString(Charsets.UTF_8)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取Uri指向的文件名
     * @param uri 文件Uri
     * @return 文件名
     */
    fun getFileNameFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "content" -> {
                    context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (cursor.moveToFirst()) {
                            cursor.getString(nameIndex)
                        } else {
                            null
                        }
                    }
                }
                "file" -> DocumentFile.fromSingleUri(context, uri)?.name
                else -> null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 检查文件是否存在
     * @param file 要检查的文件
     * @return 文件是否存在
     */
    fun fileExists(file: File): Boolean {
        return file.exists()
    }

    /**
     * 删除文件或目录
     * @param file 要删除的文件或目录
     * @return 是否删除成功
     */
    fun deleteFileOrDirectory(file: File): Boolean {
        return try {
            if (file.isDirectory) {
                file.deleteRecursively()
            } else {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}