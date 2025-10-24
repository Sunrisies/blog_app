package com.sunrise.blog.model

import com.google.gson.annotations.SerializedName

// 使用更简单的数据模型，避免复杂的嵌套泛型
data class ApiResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: DataWrapper?
) {
    // 添加类型安全的辅助方法
    fun getPosts(): List<Post> {
        return data?.posts ?: emptyList()
    }

    fun getTotalCount(): Int {
        return data?.pagination?.total ?: 0
    }

    fun isSuccessful(): Boolean = code == 200
}

// 使用简单的包装类，避免复杂的泛型嵌套
data class DataWrapper(
    @SerializedName("data") val posts: List<Post>?,
    @SerializedName("pagination") val pagination: Pagination?
)

// 其他数据类保持不变，但添加 @Keep 注解
data class Post(
    @SerializedName("id") val id: Int,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("author") val author: String,
    @SerializedName("publish_time") val publishTime: String,
    @SerializedName("update_time") val updateTime: String,
    @SerializedName("views") val views: Int,
    @SerializedName("is_top") val isTop: Boolean,
    @SerializedName("is_publish") val isPublish: Boolean,
    @SerializedName("is_hide") val isHide: Boolean,
    @SerializedName("description") val description: String,
    @SerializedName("size") val size: Int,
    @SerializedName("category") val category: Category,
    @SerializedName("tags") val tags: List<Tag>
)

data class Category(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class Tag(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class Pagination(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int
)

// 文章详情响应
data class PostDetailResponse(
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: PostDetail?
)

// 文章详情数据
data class PostDetail(
    @SerializedName("id") val id: Int,
    @SerializedName("uuid") val uuid: String,
    @SerializedName("title") val title: String,
    @SerializedName("cover") val cover: String,
    @SerializedName("author") val author: String,
    @SerializedName("content") val content: String,
    @SerializedName("publish_time") val publishTime: String,
    @SerializedName("update_time") val updateTime: String,
    @SerializedName("views") val views: Int,
    @SerializedName("is_top") val isTop: Boolean,
    @SerializedName("is_publish") val isPublish: Boolean,
    @SerializedName("is_hide") val isHide: Boolean,
    @SerializedName("description") val description: String,
    @SerializedName("size") val size: Int,
    @SerializedName("category") val category: Category,
    @SerializedName("tags") val tags: List<Tag>
)