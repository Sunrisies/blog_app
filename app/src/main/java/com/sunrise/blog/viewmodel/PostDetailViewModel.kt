package com.sunrise.blog.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sunrise.blog.model.PostDetail
import com.sunrise.blog.network.RetrofitClient
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {
    var state by mutableStateOf(PostDetailState())
        private set

    fun loadPostDetail(uuid: String) {
        if (state.isLoading) return

        viewModelScope.launch {
            state = state.copy(
                isLoading = true,
                error = null
            )

            try {
                val response = RetrofitClient.apiService.getPostDetail(uuid)

                if (response.code == 200) {
                    state = state.copy(
                        postDetail = response.data,
                        isLoading = false,
                        error = null
                    )
                } else {
                    state = state.copy(
                        isLoading = false,
                        error = "获取文章详情失败: ${response.message}"
                    )
                }
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.SocketTimeoutException -> "网络连接超时"
                    is java.net.UnknownHostException -> "网络不可用"
                    is com.google.gson.JsonSyntaxException -> "数据解析错误"
                    else -> "网络错误: ${e.message ?: "未知错误"}"
                }

                state = state.copy(
                    isLoading = false,
                    error = errorMessage
                )

                e.printStackTrace()
            }
        }
    }
}

data class PostDetailState(
    val postDetail: PostDetail? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)