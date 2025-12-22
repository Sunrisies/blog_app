package com.sunrise.blog.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.MediaStore
import androidx.documentfile.provider.DocumentFile
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文件存储管理器
 * 提供创建目录、创建文件和读取文件的功能
 */
class FileStorageManager(private val context: Context) {

    /**
     * 在Download目录下创建指定路径的文件（推荐使用）
     * @param directoryPath 目录路径（相对于Download目录，如 "HOVER" 或 "MyApp/Logs"）
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInDownloadDir(directoryPath: String, fileName: String, content: String): Result<String> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            // 获取Download目录
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                val mkdirsSuccess = downloadDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建Download目录: ${downloadDir.absolutePath}"))
                }
            }
            
            // 创建指定目录路径（在Download目录下）
            val targetDir = File(downloadDir, directoryPath)
            if (!targetDir.exists()) {
                val mkdirsSuccess = targetDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建目录: ${targetDir.absolutePath}"))
                }
            }
            
            // 创建文件
            val file = File(targetDir, fileName)
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
     * 读取Download目录下指定路径的文件内容
     * @param directoryPath 目录路径（相对于Download目录）
     * @param fileName 文件名
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFromDownloadDir(directoryPath: String, fileName: String): String? {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadDir, directoryPath)
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
     * 列出Download目录下指定路径的所有文件
     * @param directoryPath 目录路径（相对于Download目录）
     * @return 文件列表
     */
    fun listFilesInDownloadDir(directoryPath: String): List<File> {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadDir, directoryPath)
            
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
     * 删除Download目录下指定路径的文件
     * @param directoryPath 目录路径（相对于Download目录）
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFromDownloadDir(directoryPath: String, fileName: String): Boolean {
        return try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val targetDir = File(downloadDir, directoryPath)
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
     * 在指定目录下创建文件（保留原有方法，但建议使用Download目录方法）
     * @param directoryPath 目录路径（相对于根目录）
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInSpecifiedDir(directoryPath: String, fileName: String, content: String): Result<String> {
        // 建议使用 createFileInDownloadDir 代替，避免权限问题
        return try {
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }
            
            val rootDir = Environment.getExternalStorageDirectory()
            val targetDir = File(rootDir, directoryPath)
            if (!targetDir.exists()) {
                val mkdirsSuccess = targetDir.mkdirs()
                if (!mkdirsSuccess) {
                    return Result.failure(Exception("无法创建目录: ${targetDir.absolutePath}"))
                }
            }
            
            val file = File(targetDir, fileName)
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
     * 读取指定目录下的文件内容
     * @param directoryPath 目录路径（相对于根目录）
     * @param fileName 文件名
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFileFromSpecifiedDir(directoryPath: String, fileName: String): String? {
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
     * 列出指定目录下的所有文件和目录
     * @param directoryPath 目录路径（相对于根目录）
     * @return 文件列表
     */
    fun listFilesInSpecifiedDir(directoryPath: String): List<File> {
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
     * 删除指定目录下的文件
     * @param directoryPath 目录路径（相对于根目录）
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFileFromSpecifiedDir(directoryPath: String, fileName: String): Boolean {
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
     * 在根目录下创建文件（保留原有方法，但建议使用指定目录方法）
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件路径，如果失败则返回null
     */
    fun createFileInRootDir(fileName: String, content: String): Result<String> {
        // 建议使用 createFileInSpecifiedDir 代替
        return createFileInSpecifiedDir("", fileName, content)
    }

    /**
     * 读取根目录下的文件内容（保留原有方法，但建议使用指定目录方法）
     * @param fileName 文件名
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFileFromRootDir(fileName: String): String? {
        // 建议使用 readFileFromSpecifiedDir 代替
        return readFileFromSpecifiedDir("", fileName)
    }

    /**
     * 列出根目录下的所有文件和目录（保留原有方法，但建议使用指定目录方法）
     * @return 文件列表
     */
    fun listFilesInRootDir(): List<File> {
        // 建议使用 listFilesInSpecifiedDir 代替
        return listFilesInSpecifiedDir("")
    }

    /**
     * 删除根目录下的文件（保留原有方法，但建议使用指定目录方法）
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFileFromRootDir(fileName: String): Boolean {
        // 建议使用 deleteFileFromSpecifiedDir 代替
        return deleteFileFromSpecifiedDir("", fileName)
    }

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

    /**
     * 使用MediaStore在根目录下创建目录和文件（推荐用于根目录操作）
     * @param directoryPath 目录路径（相对于根目录，如 "HOVER" 或 "MyApp/Logs"）
     * @param fileName 文件名
     * @param content 文件内容
     * @return 创建的文件URI，如果失败则返回null
     */
    fun createFileInRootDirWithMediaStore(directoryPath: String, fileName: String, content: String): Result<Uri> {
        return try {
            // 检查外部存储是否可用
            if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                return Result.failure(Exception("外部存储不可用"))
            }

            // 创建目录路径（在MediaStore中使用相对路径）
            val relativePath = if (directoryPath.isNotEmpty()) {
                directoryPath
            } else {
                ""
            }

            // 创建文件的ContentValues
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
            }

            // 插入文件到MediaStore
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            
            if (uri != null) {
                // 写入文件内容
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                    outputStream.flush()
                }
                
                Result.success(uri)
            } else {
                Result.failure(Exception("无法创建文件"))
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
     * 使用MediaStore读取根目录下指定路径的文件内容
     * @param directoryPath 目录路径（相对于根目录）
     * @param fileName 文件名
     * @return 文件内容字符串，如果失败则返回null
     */
    fun readFromRootDirWithMediaStore(directoryPath: String, fileName: String): String? {
        return try {
            val resolver = context.contentResolver
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(fileName, directoryPath)
            
            val projection = arrayOf(MediaStore.MediaColumns._ID)
            val cursor = resolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
                    val uri = Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
                    
                    resolver.openInputStream(uri)?.use { inputStream ->
                        return inputStream.readBytes().toString(Charsets.UTF_8)
                    }
                }
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 使用MediaStore列出根目录下指定路径的所有文件
     * @param directoryPath 目录路径（相对于根目录）
     * @return 文件列表（使用虚拟File对象，包含路径信息）
     */
    fun listFilesInRootDirWithMediaStore(directoryPath: String): List<File> {
        return try {
            val resolver = context.contentResolver
            val selection = "${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(directoryPath)
            
            val projection = arrayOf(MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.RELATIVE_PATH)
            val cursor = resolver.query(
                MediaStore.Files.getContentUri("external"),
                projection,
                selection,
                selectionArgs,
                null
            )
            
            val fileList = mutableListOf<File>()
            cursor?.use {
                while (it.moveToNext()) {
                    val displayName = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    val relativePath = it.getString(it.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                    
                    // 创建虚拟File对象用于显示
                    val virtualFile = File(relativePath, displayName)
                    fileList.add(virtualFile)
                }
            }
            
            fileList
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * 使用MediaStore删除根目录下指定路径的文件
     * @param directoryPath 目录路径（相对于根目录）
     * @param fileName 文件名
     * @return 是否删除成功
     */
    fun deleteFromRootDirWithMediaStore(directoryPath: String, fileName: String): Boolean {
        return try {
            val resolver = context.contentResolver
            val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} = ? AND ${MediaStore.MediaColumns.RELATIVE_PATH} = ?"
            val selectionArgs = arrayOf(fileName, directoryPath)
            
            val deletedCount = resolver.delete(
                MediaStore.Files.getContentUri("external"),
                selection,
                selectionArgs
            )
            
            deletedCount > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 检查并创建目录（使用MediaStore方式）
     * @param directoryPath 目录路径（相对于根目录）
     * @return 是否成功
     */
    fun ensureDirectoryExists(directoryPath: String): Boolean {
        return try {
            // 通过创建一个隐藏文件来确保目录存在
            val tempFileName = ".keep"
            val result = createFileInRootDirWithMediaStore(directoryPath, tempFileName, "")
            
            if (result.isSuccess) {
                // 创建成功后删除临时文件
                deleteFromRootDirWithMediaStore(directoryPath, tempFileName)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
