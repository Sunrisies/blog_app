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