package com.hmyh.androidstarter.feature.post.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PostDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String,
)
