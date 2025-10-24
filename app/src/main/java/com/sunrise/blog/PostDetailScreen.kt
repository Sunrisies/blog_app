package com.sunrise.blog

import android.util.Log
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.sunrise.blog.viewmodel.PostDetailViewModel
import io.noties.markwon.Markwon
import io.noties.markwon.image.coil.CoilImagesPlugin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postUuid: String,
    navController: NavController
) {
    val viewModel: PostDetailViewModel = viewModel()
    val state = viewModel.state

    // 加载文章详情
    LaunchedEffect(postUuid) {
        Log.d("PostDetail", "Loading post with UUID: $postUuid")
        viewModel.loadPostDetail(postUuid)
    }

    // 获取 Context
    val context = LocalContext.current

    // 创建 Markwon 实例 - 修复版本
    val markwon = remember {
        Markwon.builder(context)
            .usePlugin(CoilImagesPlugin.create(context))
            .build()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.postDetail?.title ?: "文章详情",
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
        when {
            state.isLoading -> {
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
                        CircularProgressIndicator()
                        Text("加载中...")
                    }
                }
            }

            state.error != null -> {
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
                            text = state.error ?: "未知错误",
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { viewModel.loadPostDetail(postUuid) }) {
                            Text("重试")
                        }
                    }
                }
            }

            state.postDetail != null -> {
                val post = state.postDetail!!

                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // 文章封面
                    AsyncImage(
                        model = post.cover,
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
                        // 标题
                        Text(
                            text = post.title,
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
                                text = "作者: ${post.author}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )

                            Text(
                                text = "阅读: ${post.views}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF666666)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 分类和标签
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 分类
                            Text(
                                text = "分类: ${post.category.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )

                            // 标签
                            if (post.tags.isNotEmpty()) {
                                Text(
                                    text = "标签: ${post.tags.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF388E3C)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "发布时间: ${post.publishTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF999999),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // 文章描述
                        Text(
                            text = post.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF666666),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Markdown 内容
                        Text(
                            text = "文章内容",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Markdown 渲染
                        AndroidView(
                            factory = { context ->
                                val textView = android.widget.TextView(context)
                                textView.textSize = 16f
                                textView.setTextColor(android.graphics.Color.parseColor("#333333"))
                                textView.setLineSpacing(1.4f, 1.4f)
                                textView
                            },
                            update = { textView ->
                                markwon.setMarkdown(textView, post.content)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
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

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无数据")
                }
            }
        }
    }
}