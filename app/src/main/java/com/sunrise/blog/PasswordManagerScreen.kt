package com.sunrise.blog

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sunrise.blog.model.PasswordItem
import com.sunrise.blog.viewmodel.ImportResult
import com.sunrise.blog.viewmodel.PasswordManagerViewModel
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun PasswordManagerScreen(navController: NavController) {
    val viewModel: PasswordManagerViewModel = viewModel()
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    // 使用 Column 替代 Scaffold
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 自定义顶部栏
        CustomTopBar(navController = navController, viewModel = viewModel)

        // 内容区域
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text("加载中...")
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                PasswordStats(viewModel = viewModel)
                PasswordList(
                    viewModel = viewModel,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }

        // 浮动操作按钮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加密码")
            }
        }

        // 对话框部分保持不变
        if (viewModel.showEditDialog) {
            val currentPassword = viewModel.selectedPassword ?: PasswordItem()
            PasswordEditDialog(
                password = currentPassword,
                onSave = { updatedPassword ->
                    if (currentPassword.id.isEmpty()) {
                        viewModel.addPassword(updatedPassword)
                    } else {
                        viewModel.updatePassword(updatedPassword)
                    }
                    viewModel.closeEditDialog()
                },
                onDismiss = { viewModel.closeEditDialog() }
            )
        }

        if (viewModel.showDeleteDialog) {
            DeleteConfirmDialog(viewModel = viewModel)
        }

        if (viewModel.showImportDialog) {
            ImportDialog(viewModel = viewModel)
        }

        // 导入结果提示
        LaunchedEffect(viewModel.importResult) {
            viewModel.importResult.value?.let { result ->
                when (result) {
                    is ImportResult.Success -> {
                        Toast.makeText(
                            context,
                            "导入成功: 新增 ${result.added} 条, 更新 ${result.updated} 条",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is ImportResult.Error -> {
                        Toast.makeText(
                            context,
                            "导入失败: ${result.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                viewModel.clearImportResult()
            }
        }
    }
}

@Composable
fun CustomTopBar(navController: NavController, viewModel: PasswordManagerViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // 顶部栏内容
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // 标准 Material Design 高度
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧：返回按钮和标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }

                Text(
                    "密码管理器",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // 右侧：操作按钮
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.showImportDialog(true) }
                ) {
                    Icon(Icons.Default.FileDownload, contentDescription = "导入")
                }
                IconButton(
                    onClick = {
                        val json = viewModel.exportToJson()
                        Toast.makeText(context, "已导出 ${viewModel.passwords.size} 条密码", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "导出")
                }
            }
        }

        // 底部装饰线
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}

@Composable
fun PasswordStats(viewModel: PasswordManagerViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 总数量
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = viewModel.passwords.size.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "总数量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 分隔线
            Divider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            // 分类数
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = viewModel.passwords.distinctBy { it.category }.size.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "分类数",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PasswordList(viewModel: PasswordManagerViewModel, modifier: Modifier = Modifier) {
    val passwords = viewModel.passwords

    if (passwords.isEmpty()) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "空状态",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "暂无密码记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "点击右下角 + 按钮添加第一条密码",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(passwords, key = { it.id }) { password ->
                PasswordItemCard(
                    password = password,
                    onEdit = { viewModel.selectPassword(it) },
                    onDelete = {
                        viewModel.selectPassword(it)
                        viewModel.showDeleteDialog(true)
                    }
                )
            }
        }
    }
}

@Composable
fun PasswordItemCard(
    password: PasswordItem,
    onEdit: (PasswordItem) -> Unit,
    onDelete: (PasswordItem) -> Unit
) {
    var showPassword by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit(password) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题和分类
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = password.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // 分类标签
                if (password.category.isNotEmpty()) {
                    Text(
                        text = password.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 账号信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = "账号",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = password.account,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 密码信息
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = "密码",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showPassword) password.password else "•".repeat(12),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showPassword = !showPassword },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "隐藏密码" else "显示密码",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 网站和备注
            if (password.website.isNotEmpty() || password.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                if (password.website.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = "网站",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = password.website,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (password.notes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = password.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 操作按钮
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box {
                    IconButton(
                        onClick = { expanded = true }
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑") },
                            onClick = {
                                expanded = false
                                onEdit(password)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                expanded = false
                                onDelete(password)
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        )
                    }
                }
            }
        }
    }
}

// 对话框部分保持不变
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordEditDialog(
    password: PasswordItem,
    onSave: (PasswordItem) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(password.name) }
    var account by remember { mutableStateOf(password.account) }
    var passwordText by remember { mutableStateOf(password.password) }
    var website by remember { mutableStateOf(password.website) }
    var notes by remember { mutableStateOf(password.notes) }
    var category by remember { mutableStateOf(password.category) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (password.id.isEmpty()) "添加密码" else "编辑密码") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = name.isEmpty()
                )

                OutlinedTextField(
                    value = account,
                    onValueChange = { account = it },
                    label = { Text("账号 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = account.isEmpty()
                )

                OutlinedTextField(
                    value = passwordText,
                    onValueChange = { passwordText = it },
                    label = { Text("密码 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    isError = passwordText.isEmpty()
                )

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("网站") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotEmpty() && account.isNotEmpty() && passwordText.isNotEmpty()) {
                        val updatedPassword = password.copy(
                            name = name,
                            account = account,
                            password = passwordText,
                            website = website,
                            notes = notes,
                            category = if (category.isEmpty()) "默认" else category
                        )
                        onSave(updatedPassword)
                    }
                },
                enabled = name.isNotEmpty() && account.isNotEmpty() && passwordText.isNotEmpty()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(viewModel: PasswordManagerViewModel) {
    val password = viewModel.selectedPassword

    AlertDialog(
        onDismissRequest = { viewModel.showDeleteDialog(false) },
        title = { Text("确认删除") },
        text = {
            Text("确定要删除 \"${password?.name}\" 吗？此操作不可恢复。")
        },
        confirmButton = {
            TextButton(
                onClick = {
                    password?.let { viewModel.deletePassword(it) }
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("删除")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showDeleteDialog(false) }) {
                Text("取消")
            }
        }
    )
}

@Composable
fun ImportDialog(viewModel: PasswordManagerViewModel) {
    var jsonInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { viewModel.showImportDialog(false) },
        title = { Text("导入密码") },
        text = {
            Column {
                Text("请输入 JSON 格式的密码数据：", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jsonInput,
                    onValueChange = { jsonInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    placeholder = { Text("粘贴 JSON 数据...") },
                    singleLine = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "JSON 格式示例:\n" +
                            "{\n" +
                            "  \"passwords\": [\n" +
                            "    {\"name\": \"示例\", \"account\": \"user\", \"password\": \"pass\"}\n" +
                            "  ]\n" +
                            "}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (jsonInput.isNotEmpty()) {
                        viewModel.importFromJson(jsonInput)
                        viewModel.showImportDialog(false)
                    }
                },
                enabled = jsonInput.isNotEmpty()
            ) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.showImportDialog(false) }) {
                Text("取消")
            }
        }
    )
}