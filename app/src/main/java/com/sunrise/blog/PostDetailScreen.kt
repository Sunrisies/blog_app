package com.sunrise.blog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postUuid: String,
    navController: NavController
) {
    // 这里应该根据 postUuid 从 API 获取文章详情
    // 暂时先显示一个模拟的详情页面

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "文章详情",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 文章封面
            AsyncImage(
                model = "https://vip.chaoyang1024.top/uploads/2025/10/13/maple-6678635.jpg",
                contentDescription = "文章封面",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // 文章内容区域
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "文章 ID: $postUuid",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 文章元信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "作者: 朝阳",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )

                    Text(
                        text = "阅读: 1234",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "发布时间: 2025-10-10 15:21:22",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 文章内容
                Text(
                    text = """
                        这是文章 $postUuid 的详细内容...
                        
                        这里可以显示完整的文章内容，包括文字、图片、代码块等。
                        
                        用户可以在全屏模式下专注于阅读，不受底部导航栏的干扰。
                        
                        阅读完成后，可以点击左上角的返回按钮回到文章列表。
                        
                        这种设计模式在大多数阅读类应用中都很常见。
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyLarge,
                    lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.6,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = { /* 点赞操作 */ }) {
                        Text("点赞")
                    }

                    Button(onClick = { /* 收藏操作 */ }) {
                        Text("收藏")
                    }

                    Button(onClick = { /* 分享操作 */ }) {
                        Text("分享")
                    }
                }
            }
        }
    }
}