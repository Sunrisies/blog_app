package com.sunrise.blog

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.TextField
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.sunrise.blog.util.FileStorageManager
import java.io.File
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Add
import android.os.Environment
import android.widget.Toast
import androidx.compose.material.icons.filled.Save
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResultCallback
import androidx.core.content.ContextCompat
import android.provider.MediaStore

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: Int , // 可以使用本地图标资源
    val route: String, // 导航路由
    val color: Long // 主题色
)
@Composable
fun ToolsScreen(navController: NavController) {
    val tools = remember {
        listOf(
            ToolItem(
                id = "json_formatter",
                name = "JSON 格式化",
                description = "美化和格式化 JSON 数据",
                iconResId = android.R.drawable.ic_menu_edit, // 使用系统图标示例
                route = "json_formatter",
                color = 0xFF4CAF50
            ),
            ToolItem(
                id = "password_manager",
                name = "密码管理器",
                description = "安全存储和管理您的密码",
                iconResId =android.R.drawable.ic_lock_lock, // 使用锁图标
                route = "password_manager",
                color = 0xFF4CAF50
            ),
            ToolItem(
              id = "metronome",
                name = "步频器",
                description = "步频器",
                iconResId = android.R.drawable.ic_menu_rotate,
                route = "metronome",
                color = 0xFF4CAF50
            ),

            ToolItem(
                id = "video_player",
                name = "视频播放器",
                description = "支持RTSP/RTMP等流媒体协议",
                iconResId = android.R.drawable.ic_media_play,
                route = "video_player",
                color = 0xFFE91E63
            ),
            ToolItem(
                id = "base64",
                name = "Base64 编解码",
                description = "Base64 编码和解码工具",
                iconResId = android.R.drawable.ic_menu_sort_by_size,
                route = "base64_tool",
                color = 0xFF2196F3
            ),
            ToolItem(
                id = "timestamp",
                name = "时间戳转换",
                description = "时间戳与日期时间互转",
                iconResId = android.R.drawable.ic_menu_month,
                route = "timestamp_tool",
                color = 0xFFFF9800
            ),
            ToolItem(
                id = "color_picker",
                name = "颜色选择器",
                description = "十六进制颜色代码选择",
                iconResId = android.R.drawable.ic_menu_gallery,
                route = "color_picker",
                color = 0xFFE91E63
            ),
            ToolItem(
                id = "qr_generator",
                name = "二维码生成",
                description = "生成自定义二维码",
                iconResId = android.R.drawable.ic_menu_camera,
                route = "qr_generator",
                color = 0xFF9C27B0
            ),
            ToolItem(
                id = "markdown_editor",
                name = "Markdown 编辑器",
                description = "在线 Markdown 编辑和预览",
                iconResId = android.R.drawable.ic_menu_edit,
                route = "markdown_editor",
                color = 0xFF607D8B
            ),
            ToolItem(
                id = "unit_converter",
                name = "单位换算",
                description = "长度、重量、温度等单位换算",
                iconResId = android.R.drawable.ic_menu_rotate,
                route = "unit_converter",
                color = 0xFF795548
            ),
            ToolItem(
                id = "regex_tester",
                name = "正则表达式测试",
                description = "在线正则表达式测试工具",
                iconResId = android.R.drawable.ic_menu_search,
                route = "regex_tester",
                color = 0xFF3F51B5
            ),
            ToolItem(
                id = "file_manager",
                name = "文件管理器",
                description = "创建目录、文件和读取文件内容",
                iconResId = android.R.drawable.ic_menu_save,
                route = "file_manager",
                color = 0xFF9E9E9E
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 标题栏
        Text(
            text = "开发者工具",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(24.dp, 14.dp, 24.dp, 14.dp)
        )

        Text(
            text = "精选实用开发工具，提升开发效率",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 工具列表
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(tools, key = { it.id }) { tool ->
                ToolItemCard(
                    tool = tool,
                    onClick = {
                        // 导航到对应的工具页面
                        navController.navigate(tool.route)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // 更多工具提示
                Text(
                    text = "更多工具正在开发中...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ToolItemCard(tool: ToolItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp), // 增加外边距，使卡片之间有间隔
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant, // 使用较深的背景色
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant // 使用较浅的内容色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp) // 增加阴影效果
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // 增加内边距
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp) // 调整间距
        ) {
            // 图标容器
            Box(
                modifier = Modifier
                    .size(48.dp) // 调整图标大小
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(tool.color)), // 使用工具的颜色
                contentAlignment = Alignment.Center
            ) {
                // 使用图标或简化的文本
                Text(
                    text = tool.name.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall // 调整字体大小
                )
            }

            // 文本内容
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleSmall, // 调整字体大小
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall, // 调整字体大小
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 右侧箭头
            Icon(
                imageVector = Icons.Default.ArrowCircleRight,
                contentDescription = "进入工具",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp) // 调整箭头大小
            )
        }
    }
}

// 文件管理器屏幕
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(navController: NavController) {
    val context = LocalContext.current
    val fileStorageManager = remember { FileStorageManager(context) }
    
    var directoryName by rememberSaveable { mutableStateOf("") }
    var externalDirectoryName by rememberSaveable { mutableStateOf("") }
    var externalRootDirectoryName by rememberSaveable { mutableStateOf("") }
    var fileName by rememberSaveable { mutableStateOf("") }
    var rootFileName by rememberSaveable { mutableStateOf("") }
    var externalFileName by rememberSaveable { mutableStateOf("") }
    var externalRootFileName by rememberSaveable { mutableStateOf("") }
    var fileContent by rememberSaveable { mutableStateOf("") }
    var operationResult by rememberSaveable { mutableStateOf("") }
    var createdDirectory by remember { mutableStateOf<File?>(null) }
    var createdFile by remember { mutableStateOf<File?>(null) }
    var createdRootFile by remember { mutableStateOf<File?>(null) }
    var rootFiles by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var selectedSystemFileUri by remember { mutableStateOf<Uri?>(null) }
    
    // 请求存储权限
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            operationResult = "存储权限已授予"
            Toast.makeText(context, "存储权限已授予", Toast.LENGTH_SHORT).show()
        } else {
            operationResult = "存储权限被拒绝，请在设置中手动授予权限"
            Toast.makeText(context, "存储权限被拒绝，请在设置中手动授予权限", Toast.LENGTH_LONG).show()
        }
    }
    
    // 系统文件选择器
    val selectFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            selectedSystemFileUri = selectedUri
            val fileName = fileStorageManager.getFileNameFromUri(selectedUri)
            val content = fileStorageManager.readFileFromUri(selectedUri)
            
            if (content != null) {
                fileContent = content
                operationResult = "已从系统文件选择器加载文件: ${fileName ?: selectedUri.toString()}"
            } else {
                operationResult = "无法读取所选文件的内容"
            }
        }
    }
    
    // SAF创建文件
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { fileUri ->
            try {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(fileContent.toByteArray())
                }
                operationResult = "文件已成功保存到: $fileUri"
            } catch (e: Exception) {
                operationResult = "保存文件失败: ${e.message}"
            }
        }
    }
    
    // 检查并请求存储权限
    fun checkAndRequestStoragePermission() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                // Android 10及以上版本不需要请求WRITE_EXTERNAL_STORAGE权限
                operationResult = "Android 10及以上版本不需要额外的存储权限"
            }
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                operationResult = "存储权限已授予"
            }
            else -> {
                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }
    
    // 刷新文件列表
    fun refreshFileList() {
        rootFiles = fileStorageManager.listRootFiles()
    }
    
    // 初始化时加载文件列表并检查权限
    LaunchedEffect(Unit) {
        refreshFileList()
        checkAndRequestStoragePermission()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("文件管理器") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "文件操作演示",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "使用此工具可以创建目录、创建文件和读取文件内容",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 系统文件选择部分
            item {
                Text(
                    text = "系统文件操作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Button(
                    onClick = {
                        selectFileLauncher.launch("*/*")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("选择系统文件")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (externalFileName.isNotEmpty() && fileContent.isNotEmpty()) {
                            createDocumentLauncher.launch(externalFileName)
                        } else {
                            operationResult = "请提供文件名和内容"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalFileName.isNotEmpty() && fileContent.isNotEmpty()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("使用系统文件选择器保存文件")
                }
            }
            
            // 外部存储根目录操作部分
            item {
                Text(
                    text = "外部存储根目录操作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = externalRootDirectoryName,
                    onValueChange = { externalRootDirectoryName = it },
                    label = { Text("外部存储根目录中的目录名") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (externalRootDirectoryName.isNotEmpty()) {
                            val result = fileStorageManager.createDirectoryInExternalRoot(
                                externalRootDirectoryName
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    "成功在外部存储根目录创建子目录: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "在外部存储根目录创建子目录失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "在外部存储根目录创建子目录失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供目录名"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalRootDirectoryName.isNotEmpty()
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在外部存储根目录创建子目录")
                }
            }
            
            item {
                OutlinedTextField(
                    value = externalRootFileName,
                    onValueChange = { externalRootFileName = it },
                    label = { Text("外部存储根目录中的文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (externalRootFileName.isNotEmpty() && fileContent.isNotEmpty()) {
                            val result = fileStorageManager.createFileInExternalRoot(
                                externalRootFileName, 
                                fileContent
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    "成功在外部存储根目录创建文件: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "在外部存储根目录创建文件失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "在外部存储根目录创建文件失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供文件名和内容"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalRootFileName.isNotEmpty() && fileContent.isNotEmpty()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在外部存储根目录创建文件")
                }
            }
            
            // 外部存储公共目录操作部分
            item {
                Text(
                    text = "外部存储公共目录操作",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = externalDirectoryName,
                    onValueChange = { externalDirectoryName = it },
                    label = { Text("外部存储中的目录名") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (externalDirectoryName.isNotEmpty()) {
                            val result = fileStorageManager.createDirectoryInExternalPublicDir(
                                externalDirectoryName,
                                Environment.DIRECTORY_DOWNLOADS
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    "成功在Download目录创建子目录: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "在Download目录创建子目录失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "在Download目录创建子目录失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供目录名"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalDirectoryName.isNotEmpty()
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在Download目录创建子目录")
                }
            }
            
            item {
                OutlinedTextField(
                    value = externalFileName,
                    onValueChange = { externalFileName = it },
                    label = { Text("外部存储中的文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (externalFileName.isNotEmpty() && fileContent.isNotEmpty()) {
                            val result = fileStorageManager.createFileInExternalPublicDir(
                                externalFileName, 
                                fileContent, 
                                Environment.DIRECTORY_DOWNLOADS
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    "成功在Download目录创建文件: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "在Download目录创建文件失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "在Download目录创建文件失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供文件名和内容"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalFileName.isNotEmpty() && fileContent.isNotEmpty()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在Download目录创建文件")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (externalFileName.isNotEmpty() && fileContent.isNotEmpty()) {
                            val result = fileStorageManager.createFileInExternalPublicDir(
                                externalFileName, 
                                fileContent, 
                                Environment.DIRECTORY_DOCUMENTS
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    "成功在Documents目录创建文件: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "在Documents目录创建文件失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "在Documents目录创建文件失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供文件名和内容"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = externalFileName.isNotEmpty() && fileContent.isNotEmpty()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在Documents目录创建文件")
                }
            }
            
            // 文件列表部分
            item {
                Text(
                    text = "根目录文件列表",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                Button(
                    onClick = {
                        refreshFileList()
                        operationResult = "文件列表已刷新，共 ${rootFiles.size} 个项目"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("刷新文件列表")
                }
            }
            
            items(rootFiles) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedFile = file
                            // 自动读取选中文件的内容
                            if (file.isFile) {
                                val content = fileStorageManager.readFile(file)
                                fileContent = content ?: ""
                                operationResult = "已选择文件: ${file.name}"
                            } else {
                                operationResult = "已选择目录: ${file.name}"
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedFile == file) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.FileOpen,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (file.isDirectory) "目录" else "文件",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = file.absolutePath,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // 分隔线
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            
            // 在根目录下创建文件的部分
            item {
                Text(
                    text = "在应用内部存储根目录下创建文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = rootFileName,
                    onValueChange = { rootFileName = it },
                    label = { Text("应用内部存储中的文件名") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                    }
                )
            }
            
            item {
                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { fileContent = it },
                    label = { Text("文件内容") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    minLines = 3,
                    maxLines = 5
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (rootFileName.isNotEmpty()) {
                            createdRootFile = fileStorageManager.createFileInRoot(rootFileName, fileContent)
                            operationResult = if (createdRootFile != null) {
                                refreshFileList()
                                "成功在应用内部存储创建文件: ${createdRootFile?.name}"
                            } else {
                                "在应用内部存储创建文件失败"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = rootFileName.isNotEmpty()
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在应用内部存储创建文件")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (selectedFile != null && selectedFile!!.isFile) {
                            val content = fileStorageManager.readFile(selectedFile!!)
                            operationResult = if (content != null) {
                                fileContent = content
                                "已读取文件内容: ${selectedFile?.name}"
                            } else {
                                "读取文件失败"
                            }
                        } else {
                            operationResult = "请选择一个文件"
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("读取选中文件")
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (selectedFile != null) {
                            val deleted = fileStorageManager.deleteFileOrDirectory(selectedFile!!)
                            operationResult = if (deleted) {
                                refreshFileList()
                                "文件/目录已删除: ${selectedFile?.name}"
                            } else {
                                "删除文件/目录失败"
                            }
                            selectedFile = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedFile != null,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("删除选中项")
                }
            }
            
            // 分隔线
            item {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            
            // 在指定目录下创建文件的部分
            item {
                Text(
                    text = "在应用内部指定目录下创建文件",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            item {
                OutlinedTextField(
                    value = directoryName,
                    onValueChange = { directoryName = it },
                    label = { Text("目录名称") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.Folder, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (directoryName.isNotEmpty()) {
                            createdDirectory = fileStorageManager.createDirectory(directoryName)
                            operationResult = if (createdDirectory != null) {
                                refreshFileList()
                                "成功创建目录: ${createdDirectory?.name}"
                            } else {
                                "创建目录失败"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = directoryName.isNotEmpty()
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("创建目录")
                }
            }
            
            item {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("文件名称") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(Icons.Default.FileOpen, contentDescription = null)
                    }
                )
            }
            
            item {
                Button(
                    onClick = {
                        if (fileName.isNotEmpty() && createdDirectory != null) {
                            createdFile = fileStorageManager.createFile(createdDirectory!!, fileName, fileContent)
                            operationResult = if (createdFile != null) {
                                refreshFileList()
                                "成功创建文件: ${createdFile?.name}"
                            } else {
                                "创建文件失败"
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fileName.isNotEmpty() && createdDirectory != null
                ) {
                    Icon(Icons.Default.Create, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("在指定目录创建文件")
                }
            }
            
            item {
                if (operationResult.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = operationResult,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

// 创建不同的工具页面
@Composable
fun JsonFormatterScreen(navController: NavController) {
    ToolDetailScreen(
        navController = navController,
        title = "JSON 格式化",
        content = "JSON 格式化工具内容..."
    )
}

@Composable
fun Base64ToolScreen(navController: NavController) {
    ToolDetailScreen(
        navController = navController,
        title = "Base64 编解码",
        content = "Base64 编解码工具内容..."
    )
}

@Composable
fun TimestampToolScreen(navController: NavController) {
    ToolDetailScreen(
        navController = navController,
        title = "时间戳转换",
        content = "时间戳转换工具内容..."
    )
}

//@Composable
//fun VideoPlayerScreen(navController: NavController) {
//    com.sunrise.blog.VideoPlayerScreen(navController)
//}

// 通用的工具详情页面模板
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolDetailScreen(navController: NavController, title: String, content: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyLarge
                )
                CircularProgressIndicator()
                Text("功能开发中...")
            }
        }
    }
}


