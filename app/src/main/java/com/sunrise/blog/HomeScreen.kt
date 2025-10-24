package com.sunrise.blog

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.king.ultraswiperefresh.UltraSwipeRefresh
import com.king.ultraswiperefresh.indicator.classic.ClassicRefreshFooter
import com.king.ultraswiperefresh.indicator.classic.ClassicRefreshHeader
import com.king.ultraswiperefresh.rememberUltraSwipeRefreshState
import com.sunrise.blog.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun HomeScreen() {
    val viewModel: HomeViewModel = viewModel()
    val state = viewModel.state

    // 使用 UltraSwipeRefresh 的状态
    val ultraState = rememberUltraSwipeRefreshState(
        isRefreshing = state.isRefreshing,
        isLoading = state.isLoading
    )

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // 监听状态变化，同步到 UltraSwipeRefresh
    LaunchedEffect(state.isRefreshing, state.isLoading) {
        ultraState.isRefreshing = state.isRefreshing
        ultraState.isLoading = state.isLoading
    }

    // 自动隐藏刷新时间（3秒后）
    LaunchedEffect(state.showRefreshTime) {
        if (state.showRefreshTime) {
            kotlinx.coroutines.delay(3000)
            viewModel.hideRefreshTime()
        }
    }

    // 自动加载更多监听（参考 ClassicRefreshAutoLoadSample）
    LaunchedEffect(lazyListState) {
        snapshotFlow {
            val lastVisibleItem = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null && lastVisibleItem.index > state.posts.size - 5
        }.distinctUntilChanged().collect { shouldLoadMore ->
            if (shouldLoadMore && state.hasMore && !state.isLoading && !state.isRefreshing) {
                Log.d("HomeScreen", "触发自动加载更多")
                viewModel.loadMore()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        UltraSwipeRefresh(
            state = ultraState,
            onRefresh = {
                coroutineScope.launch {
                    Log.d("HomeScreen", "触发下拉刷新")
                    viewModel.refresh()
                }
            },
            onLoadMore = {
                if (state.hasMore && !state.isLoading && !state.isRefreshing) {
                    coroutineScope.launch {
                        Log.d("HomeScreen", "触发上拉加载")
                        viewModel.loadMore()
                    }
                }
            },
            modifier = Modifier.fillMaxSize(),
            loadMoreTriggerRate = 0.1f,
            loadMoreEnabled = state.hasMore,
            onCollapseScroll = {
                // 小于0时表示：由下拉刷新收起时触发的，大于0时表示：由上拉加载收起时触发的
                if (it > 0) {
                    // 指示器收起时滚动列表位置，消除视觉回弹
                    lazyListState.animateScrollBy(it)
                }
            },
            headerIndicator = { headerState ->
                ClassicRefreshHeader(headerState)
            },
            footerIndicator = { footerState ->
                if (state.hasMore) {
                    ClassicRefreshFooter(footerState)
                } else if (state.posts.isNotEmpty()) {
                    // 只在有数据时才显示"没有更多数据了"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "--- 已经到底了，没有更多内容 ---",
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // 如果没有数据，不显示任何东西
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        ) {
            LazyColumn(
                modifier = Modifier.background(color = Color.White),
                state = lazyListState,
            ) {
                // 错误提示
                state.error?.let { error ->
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "错误",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                items(state.posts) { post ->
                    PostItem(post = post)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color(0xFFF2F3F6)
                    )
                }

                // 空状态提示
                if (state.posts.isEmpty() && !state.isLoading && !state.isRefreshing) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Refresh,
                                    contentDescription = "空状态",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                )
                                Text(
                                    text = "暂无文章数据",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Button(
                                    onClick = { viewModel.refresh() }
                                ) {
                                    Text("重新加载")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 首次加载指示器
        if (state.isLoading && state.posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "正在加载文章...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(post: com.sunrise.blog.model.Post) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 标题和封面
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 封面图片
                AsyncImage(
                    model = post.cover,
                    contentDescription = "文章封面",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                // 文章信息
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = Color(0xFF333333)
                    )

                    Text(
                        text = post.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 元信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "作者: ${post.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
                )

                Text(
                    text = "阅读: ${post.views}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF999999)
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

            // 发布时间 - 修复字段名
            Text(
                text = "发布时间: ${post.publishTime}", // 注意这里改为 publishTime
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF999999),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}