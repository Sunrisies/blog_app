package com.sunrise.blog

import android.Manifest
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sunrise.blog.util.AudioRecorder
import com.sunrise.blog.util.FileStorageManager
import com.sunrise.blog.util.MicrophonePermissionManager
import com.sunrise.blog.util.FilePermissionManager
import java.io.File

data class ToolItem(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: Int,
    val route: String,
    val color: Long
)

@Composable
fun ToolsScreen(navController: NavController) {
    val tools = remember {
        listOf(
            ToolItem(
                id = "json_formatter",
                name = "JSON 格式化",
                description = "美化和格式化 JSON 数据",
                iconResId = android.R.drawable.ic_menu_edit,
                route = "json_formatter",
                color = 0xFF4CAF50
            ),
            ToolItem(
                id = "password_manager",
                name = "密码管理器",
                description = "安全存储和管理您的密码",
                iconResId = android.R.drawable.ic_lock_lock,
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
                        navController.navigate(tool.route)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

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
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(tool.color)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tool.name.take(2),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Default.ArrowCircleRight,
                contentDescription = "进入工具",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

fun isExternalStorageWritable(): Boolean {
    return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
}

// 文件管理器屏幕
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(navController: NavController) {
    val context = LocalContext.current
    val fileStorageManager = remember { FileStorageManager(context) }
    val filePermissionManager = remember { FilePermissionManager(context) }
    
    val fixedDirectoryName = "HOVER"
    
    var fileName by rememberSaveable { mutableStateOf("") }
    var fileContent by rememberSaveable { mutableStateOf("") }
    var directoryPath by rememberSaveable { mutableStateOf("HOVER") }
    var operationResult by rememberSaveable { mutableStateOf("") }
    var fileList by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var currentTab by remember { mutableStateOf(0) }
    var permissionStatus by rememberSaveable { mutableStateOf(false) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        permissionStatus = allGranted
        operationResult = if (allGranted) {
            "文件权限已授予，可以正常访问存储"
        } else {
            "文件权限被拒绝，请在设置中手动开启权限"
        }
    }
    
    fun refreshRootDirFileList() {
        if (directoryPath.isNotEmpty()) {
            fileList = fileStorageManager.listFilesInRootDirWithMediaStore(directoryPath)
        } else {
            fileList = emptyList()
        }
    }
    
    fun refreshDownloadFileList() {
        fileList = fileStorageManager.listFilesInDownloadSubDir(fixedDirectoryName)
    }
    
    LaunchedEffect(Unit) {
        permissionStatus = filePermissionManager.isFilePermissionGranted()
        
        if (isExternalStorageWritable()) {
            val root = Environment.getRootDirectory().toString()
            val mFolder = File(root, "Folder")
            if (!mFolder.exists()) {
                val res = mFolder.mkdir()
                if (res) {
                    operationResult = "已创建/确认目录: $fixedDirectoryName"
                    refreshDownloadFileList()
                } else {
                    operationResult = "创建目录失败: ${res ?: "未知错误"}"
                }
            }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (permissionStatus) Icons.Default.Check else Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (permissionStatus) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (permissionStatus) "文件权限已授予" else "文件权限未授予",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = filePermissionManager.getPermissionStatusDescription(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (!permissionStatus) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    filePermissionManager.getRequiredPermissions()
                                )
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text("获取权限")
                        }
                    }
                }
            }
            
            TabRow(
                selectedTabIndex = currentTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = currentTab == 0,
                    onClick = { 
                        currentTab = 0 
                        refreshRootDirFileList()
                        operationResult = "已切换到根目录（使用MediaStore）"
                    },
                    text = { Text("根目录") }
                )
                Tab(
                    selected = currentTab == 1,
                    onClick = { 
                        currentTab = 1 
                        refreshDownloadFileList()
                        operationResult = "已切换到Download目录"
                    },
                    text = { Text("Download目录") }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
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
                        text = when (currentTab) {
                            0 -> "在指定目录下创建和读取文件（目录会自动创建）"
                            1 -> "在Download目录中自动创建${fixedDirectoryName}目录，用于存储文件"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (currentTab == 0) {
                    item {
                        OutlinedTextField(
                            value = directoryPath,
                            onValueChange = { 
                                directoryPath = it 
                                refreshRootDirFileList()
                            },
                            label = { Text("目录路径（相对于根目录）") },
                            placeholder = { Text("例如: HOVER 或 MyFolder/SubFolder") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(Icons.Default.Folder, contentDescription = null)
                            },
                            singleLine = true,
                            supportingText = {
                                Text("使用MediaStore API，无需额外权限")
                            }
                        )
                    }
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
                                val result = when (currentTab) {
                                    0 -> {
                                        if (directoryPath.isNotEmpty()) {
                                            fileStorageManager.createFileInRootDirWithMediaStore(directoryPath, fileName, fileContent)
                                        } else {
                                            Result.failure(Exception("请先指定目录路径"))
                                        }
                                    }
                                    1 -> fileStorageManager.createFileInDownloadSubDir(fixedDirectoryName, fileName, fileContent)
                                    else -> Result.failure(Exception("未知目录"))
                                }
                                
                                operationResult = when {
                                    result.isSuccess -> {
                                        when (currentTab) {
                                            0 -> refreshRootDirFileList()
                                            1 -> refreshDownloadFileList()
                                        }
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
                        enabled = fileName.isNotEmpty() && fileContent.isNotEmpty() && (currentTab == 1 || directoryPath.isNotEmpty()) && permissionStatus
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("创建文件")
                    }
                }
                
                item {
                    Button(
                        onClick = {
                            when (currentTab) {
                                0 -> refreshRootDirFileList()
                                1 -> refreshDownloadFileList()
                            }
                            operationResult = "文件列表已刷新，共 ${fileList.size} 个项目"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = permissionStatus
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
                            .clickable(enabled = permissionStatus) {
                                selectedFile = file
                                if (file.isFile) {
                                    val content = when (currentTab) {
                                        0 -> {
                                            if (directoryPath.isNotEmpty()) {
                                                fileStorageManager.readFromRootDirWithMediaStore(directoryPath, file.name)
                                            } else {
                                                null
                                            }
                                        }
                                        1 -> fileStorageManager.readFileFromDownloadSubDir(fixedDirectoryName, file.name)
                                        else -> null
                                    }
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
                                val content = when (currentTab) {
                                    0 -> {
                                        if (directoryPath.isNotEmpty()) {
                                            fileStorageManager.readFromRootDirWithMediaStore(directoryPath, selectedFile!!.name)
                                        } else {
                                            null
                                        }
                                    }
                                    1 -> fileStorageManager.readFileFromDownloadSubDir(fixedDirectoryName, selectedFile!!.name)
                                    else -> null
                                }
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedFile != null && permissionStatus
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
                                val deleted = when (currentTab) {
                                    0 -> {
                                        if (directoryPath.isNotEmpty()) {
                                            fileStorageManager.deleteFromRootDirWithMediaStore(directoryPath, selectedFile!!.name)
                                        } else {
                                            false
                                        }
                                    }
                                    1 -> fileStorageManager.deleteFileFromDownloadSubDir(fixedDirectoryName, selectedFile!!.name)
                                    else -> false
                                }
                                operationResult = if (deleted) {
                                    when (currentTab) {
                                        0 -> refreshRootDirFileList()
                                        1 -> refreshDownloadFileList()
                                    }
                                    "文件已删除: ${selectedFile?.name}"
                                } else {
                                    "删除文件失败"
                                }
                                selectedFile = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedFile != null && permissionStatus,
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
    
    LaunchedEffect(Unit) {
        permissionStatus = microphonePermissionManager.isMicrophonePermissionGranted()
        operationResult = if (permissionStatus) {
            "麦克风权限已授予，可以开始录音"
        } else {
            "麦克风权限未授予，请点击下方按钮申请权限"
        }
    }
    
    fun startRecording() {
        if (!permissionStatus) {
            operationResult = "请先授予麦克风权限"
            return
        }
        
        try {
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
