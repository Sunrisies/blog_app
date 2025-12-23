package com.sunrise.blog

import android.os.Build
import android.os.Environment
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sunrise.blog.util.RootFileStorageManager
import com.sunrise.blog.util.PermissionTestManager
import java.io.File

/**
 * 真正的根目录文件管理器屏幕
 * 支持在 /storage/emulated/0/ 目录下直接操作文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RootFileManagerScreen(navController: NavController) {
    val context = LocalContext.current
    val rootFileStorageManager = remember { RootFileStorageManager(context) }
    val permissionTestManager = remember { PermissionTestManager(context) }
    
    var fileName by rememberSaveable { mutableStateOf("") }
    var fileContent by rememberSaveable { mutableStateOf("") }
    var directoryPath by rememberSaveable { mutableStateOf("HOVER") }
    var operationResult by rememberSaveable { mutableStateOf("") }
    var fileList by remember { mutableStateOf<List<File>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var permissionStatus by rememberSaveable { mutableStateOf("") }
    var showPermissionButton by remember { mutableStateOf(false) }
    var permissionTestInfo by rememberSaveable { mutableStateOf("") }
    
    // Get root directory path
    val rootPath = rootFileStorageManager.getRootPath()
    
    // Check permission status
    fun checkPermissionStatus() {
        permissionStatus = rootFileStorageManager.getPermissionStatus()
        showPermissionButton = !rootFileStorageManager.hasManageExternalStoragePermission() && 
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }
    
    // Refresh file list
    fun refreshFileList() {
        if (directoryPath.isNotEmpty()) {
            fileList = rootFileStorageManager.listFilesInRootDir(directoryPath)
            operationResult = "Refreshed file list, ${fileList.size} items"
        } else {
            fileList = emptyList()
            operationResult = "Please specify directory path"
        }
    }
    
    // Smart create directory
    fun smartCreateDirectory() {
        if (directoryPath.isEmpty()) {
            operationResult = "Please specify directory path first"
            return
        }
        
        // Check if already exists
        if (rootFileStorageManager.isRootDirectoryExists(directoryPath)) {
            operationResult = "Directory exists: $rootPath/$directoryPath"
            refreshFileList()
            return
        }
        
        // Try to create
        val result = rootFileStorageManager.createRootDirectory(directoryPath)
        if (result.isSuccess) {
            operationResult = "Directory created: ${result.getOrNull()}"
            refreshFileList()
        } else {
            val errorMsg = result.exceptionOrNull()?.message ?: "Creation failed"
            operationResult = "Creation failed: $errorMsg"
            
            // If Android 10+ without permission, show solution
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && 
                !rootFileStorageManager.hasManageExternalStoragePermission()) {
                operationResult += "\n\n${rootFileStorageManager.getRecommendedSolution()}"
            }
        }
    }
    
    // Try root permission creation
    fun tryRootCreation() {
        if (directoryPath.isEmpty()) {
            operationResult = "Please specify directory path first"
            return
        }
        
        if (!rootFileStorageManager.isDeviceRooted()) {
            operationResult = "Device not rooted, cannot use this feature"
            return
        }
        
        val result = rootFileStorageManager.createDirectoryWithRoot(directoryPath)
        if (result.isSuccess) {
            operationResult = "Root creation success: ${result.getOrNull()}"
            refreshFileList()
        } else {
            operationResult = "Root creation failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
        }
    }
    
    // 初始化检查权限
    LaunchedEffect(Unit) {
        checkPermissionStatus()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("根目录文件管理器") },
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
            // 权限状态卡片
            if (permissionStatus.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (rootFileStorageManager.hasManageExternalStoragePermission() || 
                            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P)
                            MaterialTheme.colorScheme.primaryContainer
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "权限状态",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = permissionStatus,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (showPermissionButton) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val intent = rootFileStorageManager.getAvailablePermissionIntent()
                                        try {
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            // 如果所有方式都失败，显示详细指南
                                            operationResult = rootFileStorageManager.getPermissionGuide()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Settings, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("打开设置")
                                }
                                
                                Button(
                                    onClick = {
                                        // 显示MIUI专用的详细指南
                                        operationResult = rootFileStorageManager.getPermissionGuide()
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("MIUI指南")
                                }
                            }
                        }
                        
                        if (rootFileStorageManager.isDeviceRooted()) {
                            Text(
                                text = "检测到Root权限",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 根目录信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "根目录路径",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = rootPath,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "当前目录: $rootPath/$directoryPath",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "目录设置",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = directoryPath,
                        onValueChange = { 
                            directoryPath = it 
                        },
                        label = { Text("目录路径（相对于根目录）") },
                        placeholder = { Text("例如: HOVER 或 MyApp/Logs") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.Folder, contentDescription = null)
                        },
                        singleLine = true,
                        supportingText = {
                            Text("创建或切换目录")
                        }
                    )
                }
                
                item {
                    Text(
                        text = "文件操作",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("文件名") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(Icons.Default.FileOpen, contentDescription = null)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
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
                    Text(
                        text = "智能目录创建",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { smartCreateDirectory() },
                            modifier = Modifier.weight(1f),
                            enabled = directoryPath.isNotEmpty()
                        ) {
                            Icon(Icons.Default.CreateNewFolder, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("智能创建")
                        }
                        
                        if (rootFileStorageManager.isDeviceRooted()) {
                            Button(
                                onClick = { tryRootCreation() },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Root创建")
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "文件操作",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                if (fileName.isNotEmpty() && fileContent.isNotEmpty() && directoryPath.isNotEmpty()) {
                                    val result = rootFileStorageManager.createFileInRootDir(
                                        directoryPath, fileName, fileContent
                                    )
                                    if (result.isSuccess) {
                                        operationResult = "文件创建成功: ${result.getOrNull()}"
                                        refreshFileList()
                                    } else {
                                        operationResult = "创建失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                                    }
                                } else {
                                    operationResult = "请填写完整信息（目录、文件名、内容）"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = fileName.isNotEmpty() && fileContent.isNotEmpty() && directoryPath.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("创建文件")
                        }
                        
                        Button(
                            onClick = {
                                refreshFileList()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("刷新列表")
                        }
                    }
                }
                
                items(fileList) { file ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                selectedFile = file
                                if (file.isFile) {
                                    val content = rootFileStorageManager.readFromRootDir(directoryPath, file.name)
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
                            Column(modifier = Modifier.weight(1f)) {
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
                    if (selectedFile != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (selectedFile!!.isFile) {
                                        val content = rootFileStorageManager.readFromRootDir(directoryPath, selectedFile!!.name)
                                        if (content != null) {
                                            fileContent = content
                                            operationResult = "已读取内容: ${selectedFile!!.name}"
                                        } else {
                                            operationResult = "读取失败"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = selectedFile != null && selectedFile!!.isFile
                            ) {
                                Icon(Icons.Default.FileOpen, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("读取内容")
                            }
                            
                            Button(
                                onClick = {
                                    if (selectedFile != null) {
                                        val deleted = rootFileStorageManager.deleteFromRootDir(
                                            directoryPath, selectedFile!!.name
                                        )
                                        if (deleted) {
                                            operationResult = "文件已删除: ${selectedFile!!.name}"
                                            selectedFile = null
                                            refreshFileList()
                                        } else {
                                            operationResult = "删除失败"
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                enabled = selectedFile != null
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("删除文件")
                            }
                        }
                    }
                }
                
                item {
                    Text(
                        text = "权限测试",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // 显示当前权限状态和详细信息
                                permissionTestInfo = permissionTestManager.getPermissionInfo()
                                operationResult = permissionTestInfo
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("检查状态")
                        }
                        
                        Button(
                            onClick = {
                                // 打开应用设置页面（用于手动关闭/打开权限）
                                val intent = permissionTestManager.openAppSettings()
                                try {
                                    context.startActivity(intent)
                                    operationResult = "已打开应用设置页面\n请手动关闭再重新打开权限，然后返回应用"
                                } catch (e: Exception) {
                                    operationResult = "无法打开设置: ${e.message}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("打开应用设置")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                // 尝试打开所有文件访问权限设置
                                val intent = permissionTestManager.openAllFilesAccessSettings()
                                try {
                                    context.startActivity(intent)
                                    operationResult = "已打开所有文件权限设置\n请手动操作"
                                } catch (e: Exception) {
                                    operationResult = "无法打开权限设置: ${e.message}\n\n${permissionTestManager.getPermissionManagementGuide()}"
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.tertiary
                            )
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("打开所有文件设置")
                        }
                        
                        Button(
                            onClick = {
                                // 显示详细的操作指南
                                operationResult = permissionTestManager.getPermissionManagementGuide()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Help, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("手动指南")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // 测试创建目录按钮（需要权限）
                    Button(
                        onClick = {
                            val result = permissionTestManager.createTestDirectory("HOVER/PermissionTest")
                            operationResult = if (result.isSuccess) {
                                "✅ ${result.getOrNull()}\n\n权限测试成功！您可以：\n1. 返回应用\n2. 再次进入此页面\n3. 观察权限状态变化"
                            } else {
                                "❌ ${result.exceptionOrNull()?.message ?: "创建失败"}\n\n这说明当前没有权限，请按上面的步骤操作"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = permissionTestManager.hasManageExternalStoragePermission()
                    ) {
                        Icon(Icons.Default.Create, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("测试创建目录（需要权限）")
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
