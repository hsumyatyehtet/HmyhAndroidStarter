package com.hmyh.androidstarter.feature.post.domain.repository

import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.feature.post.domain.model.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun observePosts(): Flow<List<Post>>
    suspend fun refresh(): AppResult<Unit>
}
