package com.sunrise.blog.network

import com.sunrise.blog.model.ApiResponse
import com.sunrise.blog.model.PostDetailResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("posts")
    suspend fun getPosts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): ApiResponse


    // 添加获取文章详情的接口
    @GET("posts/{uuid}")
    suspend fun getPostDetail(
        @Path("uuid") uuid: String
    ): PostDetailResponse
}