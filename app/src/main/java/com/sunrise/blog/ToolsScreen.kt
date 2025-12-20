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
import com.sunrise.blog.util.MicrophonePermissionManager
import com.sunrise.blog.util.AudioRecorder
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
import androidx.navigation.compose.currentBackStackEntryAsState

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
            ),
            ToolItem(
                id = "microphone_permission",
                name = "麦克风权限",
                description = "申请和管理麦克风录音权限",
                iconResId = android.R.drawable.ic_btn_speak_now,
                route = "microphone_permission",
                color = 0xFF607D8B
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
    
    // 固定的目录名
    val fixedDirectoryName = "HOVER"
    
    var fileName by rememberSaveable { mutableStateOf("") }
    var fileContent by rememberSaveable { mutableStateOf("") }
    var operationResult by rememberSaveable { mutableStateOf("") }
    var fileList by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    
    // 刷新文件列表
    fun refreshFileList() {
        fileList = fileStorageManager.listFilesInDownloadSubDir(fixedDirectoryName)
    }
    
    // 初始化时创建目录
    LaunchedEffect(Unit) {
        val result = fileStorageManager.createDirectoryInDownloadDir(fixedDirectoryName)
        if (result.isSuccess) {
            operationResult = "已创建/确认目录: $fixedDirectoryName"
            refreshFileList()
        } else {
            operationResult = "创建目录失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
        }
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

                val fixedDirectoryName目录 = ""
                Text(
                    text = "在Download目录中自动创建$fixedDirectoryName目录，用于存储文件",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("文件名") },
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
                        if (fileName.isNotEmpty() && fileContent.isNotEmpty()) {
                            val result = fileStorageManager.createFileInDownloadSubDir(
                                fixedDirectoryName,
                                fileName,
                                fileContent
                            )
                            
                            operationResult = when {
                                result.isSuccess -> {
                                    refreshFileList()
                                    "成功创建文件: ${result.getOrNull()}"
                                }
                                result.exceptionOrNull() is SecurityException -> {
                                    "权限不足，请检查存储权限设置"
                                }
                                result.exceptionOrNull() is Exception -> {
                                    "创建文件失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                }
                                else -> {
                                    "创建文件失败: 未知错误"
                                }
                            }
                        } else {
                            operationResult = "请提供文件名和内容"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = fileName.isNotEmpty() && fileContent.isNotEmpty()
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("创建文件")
                }
            }
            
            item {
                Button(
                    onClick = {
                        refreshFileList()
                        operationResult = "文件列表已刷新，共 ${fileList.size} 个项目"
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("刷新文件列表")
                }
            }
            
            items(fileList) { file ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedFile = file
                            // 自动读取选中文件的内容
                            if (file.isFile) {
                                val content = fileStorageManager.readFileFromDownloadSubDir(
                                    fixedDirectoryName,
                                    file.name
                                )
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
            
            item {
                Button(
                    onClick = {
                        if (selectedFile != null && selectedFile!!.isFile) {
                            val content = fileStorageManager.readFileFromDownloadSubDir(
                                fixedDirectoryName,
                                selectedFile!!.name
                            )
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
                            val deleted = fileStorageManager.deleteFileFromDownloadSubDir(
                                fixedDirectoryName,
                                selectedFile!!.name
                            )
                            operationResult = if (deleted) {
                                refreshFileList()
                                "文件已删除: ${selectedFile?.name}"
                            } else {
                                "删除文件失败"
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
                    Text("删除选中文件")
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

// 添加麦克风权限管理屏幕
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicrophonePermissionScreen(navController: NavController) {
    val context = LocalContext.current
    val microphonePermissionManager = remember { MicrophonePermissionManager(context) }
    val audioRecorder = remember { AudioRecorder() }
    val fileStorageManager = remember { FileStorageManager(context) }
    
    var permissionStatus by rememberSaveable { mutableStateOf(false) }
    var operationResult by rememberSaveable { mutableStateOf("") }
    var isRecording by rememberSaveable { mutableStateOf(false) }
    var recordedFilePath by rememberSaveable { mutableStateOf<String?>(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val recordAudioGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
        
        permissionStatus = recordAudioGranted
        operationResult = if (recordAudioGranted) {
            "麦克风权限已授予，可以开始录音"
        } else {
            "麦克风权限被拒绝，请在设置中手动开启权限"
        }
    }
    
    // 初始化时检查权限状态
    LaunchedEffect(Unit) {
        permissionStatus = microphonePermissionManager.isMicrophonePermissionGranted()
        operationResult = if (permissionStatus) {
            "麦克风权限已授予，可以开始录音"
        } else {
            "麦克风权限未授予，请点击下方按钮申请权限"
        }
    }
    
    // 开始录音函数
    fun startRecording() {
        if (!permissionStatus) {
            operationResult = "请先授予麦克风权限"
            return
        }
        
        try {
            // 创建录音文件路径
            val downloadDir = fileStorageManager.getExternalDownloadDir()
            val myAppDir = File(downloadDir, "MyAppDirectory")
            if (!myAppDir.exists()) {
                myAppDir.mkdirs()
            }
            
            val fileName = "recording_${System.currentTimeMillis()}.3gp"
            val filePath = File(myAppDir, fileName).absolutePath
            
            if (audioRecorder.startRecording(filePath)) {
                isRecording = true
                recordedFilePath = null
                operationResult = "正在录音中...点击停止按钮结束录音"
            } else {
                operationResult = "开始录音失败"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            operationResult = "开始录音时发生错误: ${e.message}"
        }
    }
    
    // 停止录音函数
    fun stopRecording() {
        try {
            val filePath = audioRecorder.stopRecording()
            if (filePath != null) {
                isRecording = false
                recordedFilePath = filePath
                operationResult = "录音已保存到: $filePath"
            } else {
                operationResult = "停止录音失败"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            operationResult = "停止录音时发生错误: ${e.message}"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("麦克风权限管理") },
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
                    text = "麦克风权限管理",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "为了能够录制音频，应用需要获得麦克风访问权限。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (permissionStatus) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (permissionStatus) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (permissionStatus) "权限已授予" else "权限未授予",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            item {
                Button(
                    onClick = {
                        if (!microphonePermissionManager.isMicrophonePermissionGranted()) {
                            permissionLauncher.launch(
                                microphonePermissionManager.getRequiredPermissions()
                            )
                        } else {
                            operationResult = "麦克风权限已授予，无需重复申请"
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !microphonePermissionManager.isMicrophonePermissionGranted()
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("申请麦克风权限")
                }
            }
            
            item {
                Text(
                    text = "录音功能演示",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "获得权限后，您可以使用下方按钮进行录音测试",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (isRecording) {
                                stopRecording()
                            } else {
                                startRecording()
                            }
                        },
                        enabled = permissionStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.FiberManualRecord,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isRecording) "停止录音" else "开始录音")
                    }
                }
            }
            
            item {
                Text(
                    text = "为什么需要这个权限？",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "• 录制语音笔记\n• 进行语音通话\n• 语音识别功能\n• 其他音频相关功能",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

//@Composable
//fun BottomNavigationBar1(
//    navController: NavController,
//    items: List<BottomNavItem>
//) {
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//    // 在详情页面隐藏底部导航栏
//    val skipRoutes = setOf(
//        "post_detail",
//        "password_manager",
//        "about",
//        "metronome",
//        "file_manager",
//        "microphone_permission"
//    )
//
//// 当前路由是否属于上述任一前缀
//    val skipBottomBar = skipRoutes.any { currentRoute?.startsWith(it) == true }
//    if (skipBottomBar) return
//    NavigationBar {
//        items.forEach { item ->
//            NavigationBarItem(
//                selected = currentRoute == item.route,
//                onClick = {
//                    navController.navigate(item.route) {
//                        // 清除返回栈直到首页，避免堆叠过多页面
//                        popUpTo(navController.graph.findStartDestination().id) {
//                            saveState = true
//                        }
//                        launchSingleTop = true
//                        restoreState = true
//                    }
//                },
//                icon = {
//                    Icon(
//                        imageVector = item.icon,
//                        contentDescription = item.contentDescription
//                    )
//                },
//                label = {
//                    Text(text = item.title)
//                }
//            )
//        }
//    }
//}
//
//


