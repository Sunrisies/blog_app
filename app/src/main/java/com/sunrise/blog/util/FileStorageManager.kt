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
     * 获取外部存储Download目录
     */
    fun getExternalDownloadDir(): File? {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    }

    /**
     * 在Download目录下创建指定名称的子目录
     * @param directoryName 目录名称
     * @return 创建的目录路径，如果失败则返回null
     */
    fun createDirectoryInDownloadDir(directoryName: String): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                val mkdirsSuccess = downloadDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建Download目录: ${downloadDir.absolutePath}"))
                }
            }
            
            val directory = File(downloadDir, directoryName)
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
     * 在Download目录的子目录下创建文件
     * @param subDirectoryName 子目录名称
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInDownloadSubDir(subDirectoryName: String, fileName: String, content: String): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDirectory = File(downloadDir, subDirectoryName)
            
            // 确保子目录存在
            if (!subDirectory.exists()) {
                val mkdirsSuccess = subDirectory.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建子目录: ${subDirectory.absolutePath}"))
                }
            }
            
            val file = File(subDirectory, fileName)
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
     * 列出Download目录下指定子目录中的所有文件
     * @param subDirectoryName 子目录名称
     * @return 文件列表
     */
    fun listFilesInDownloadSubDir(subDirectoryName: String): List<File> {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDirectory = File(downloadDir, subDirectoryName)
            
            if (subDirectory.exists() && subDirectory.isDirectory) {
                subDirectory.listFiles()?.toList() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 读取Download目录下指定子目录中的文件内容
     * @param subDirectoryName 子目录名称
     * @param fileName 文件名
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFileFromDownloadSubDir(subDirectoryName: String, fileName: String): String? {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDirectory = File(downloadDir, subDirectoryName)
            val file = File(subDirectory, fileName)
            
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
     * 删除Download目录下指定子目录中的文件
     * @param subDirectoryName 子目录名称
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFileFromDownloadSubDir(subDirectoryName: String, fileName: String): Boolean {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val subDirectory = File(downloadDir, subDirectoryName)
            val file = File(subDirectory, fileName)
            
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
}