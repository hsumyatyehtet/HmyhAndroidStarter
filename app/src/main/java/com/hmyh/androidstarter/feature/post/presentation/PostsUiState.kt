package com.hmyh.androidstarter.feature.post.presentation

import com.hmyh.androidstarter.feature.post.domain.model.Post

data class PostsUiState(
    val items: List<Post> = emptyList(),
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
)
