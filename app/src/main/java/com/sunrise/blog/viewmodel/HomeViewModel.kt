package com.sunrise.blog.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrise.blog.model.Post
import com.sunrise.blog.network.RetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeViewModel : ViewModel() {
    var state by mutableStateOf(HomeState())
        private set

    init {
        loadPosts()
    }

    fun loadPosts(loadMore: Boolean = false) {
        if (state.isLoading) return

        val nextPage = if (loadMore) state.currentPage + 1 else 1

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null,
                isRefreshing = !loadMore,
                isLoadingMore = loadMore,
                showRefreshTime = !loadMore
            )

            try {
                val response = RetrofitClient.apiService.getPosts(
                    page = nextPage,
                    limit = 10
                )

                // 检查响应是否成功
                if (response.code == 200) {
                    val responseData = response.data

                    if (responseData != null && responseData.posts != null && responseData.pagination != null) {
                        val currentPosts = if (loadMore) state.posts else emptyList()
                        val newPosts = currentPosts + responseData.posts

                        val currentTime = if (!loadMore) getCurrentTime() else state.lastRefreshTime
                        // 正确计算是否还有更多数据
                        val currentLoadedCount = newPosts.size
                        val totalItems = responseData.pagination.total
                        val hasMoreData = (nextPage - 1) * 10 <  totalItems
                        state = state.copy(
                            posts = newPosts,
                            currentPage = nextPage,
                            hasMore = hasMoreData,
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = null,
                            lastRefreshTime = currentTime,
                            showRefreshTime = !loadMore
                        )
                        println("=== 分页调试信息 ===")
                        println("请求页码: $nextPage")
                        println("本次返回数据量: ${responseData.posts.size}")
                        println("当前总数据量: $currentLoadedCount")
                        println("API返回总数量: $totalItems")
                        println("计算hasMore: $hasMoreData")
                        println("计算公式: $nextPage * 10 < $totalItems = ${nextPage * 10 < totalItems}")
                        println("===================")
                    } else {
                        state = state.copy(
                            isLoading = false,
                            isRefreshing = false,
                            isLoadingMore = false,
                            error = "数据格式错误",
                            showRefreshTime = false
                        )
                    }
                } else {
                    state = state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = "请求失败: ${response.message}",
                        showRefreshTime = false
                    )
                }
            } catch (e: Exception) {
                // 更详细的错误信息
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "网络连接超时"
                    is java.net.UnknownHostException -> "网络不可用"
                    is com.google.gson.JsonSyntaxException -> "数据解析错误"
                    else -> "网络错误: ${e.message ?: "未知错误"}"
                }

                state = state.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    error = errorMessage,
                    showRefreshTime = false
                )

                // 打印详细错误日志
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        loadPosts(loadMore = false)
    }

    fun loadMore() {
        if (state.hasMore && !state.isLoading && !state.isRefreshing) {
            loadPosts(loadMore = true)
        }
    }

    fun hideRefreshTime() {
        state = state.copy(showRefreshTime = false)
    }

    private fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}

data class HomeState(
    val posts: List<Post> = emptyList(),
    val currentPage: Int = 1,
    val hasMore: Boolean = true,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: String = "",
    val showRefreshTime: Boolean = false
)