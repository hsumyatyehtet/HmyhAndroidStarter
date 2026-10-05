package com.hmyh.androidstarter.feature.post.domain.usecase

import com.hmyh.androidstarter.feature.post.domain.model.Post
import com.hmyh.androidstarter.feature.post.domain.repository.PostRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObservePostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    operator fun invoke(): Flow<List<Post>> = repository.observePosts()
}
