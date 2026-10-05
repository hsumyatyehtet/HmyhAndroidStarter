package com.hmyh.androidstarter.feature.post.presentation

import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.feature.post.domain.model.Post
import com.hmyh.androidstarter.feature.post.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakePostRepository(
    private val posts: List<Post>,
    private val refreshResult: AppResult<Unit> = AppResult.Success(Unit),
) : PostRepository {
    override fun observePosts(): Flow<List<Post>> = flowOf(posts)

    override suspend fun refresh(): AppResult<Unit> = refreshResult
}
