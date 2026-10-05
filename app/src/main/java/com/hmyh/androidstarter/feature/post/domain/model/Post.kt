package com.hmyh.androidstarter.feature.post.domain.model

data class Post(
    val id: Int,
    val userId: Int,
    val title: String,
    val body: String,
)
