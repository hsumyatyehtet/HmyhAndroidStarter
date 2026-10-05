package com.hmyh.androidstarter.feature.post.data.remote

import retrofit2.http.GET

interface PostApi {
    @GET("posts")
    suspend fun getPosts(): List<PostDto>
}
