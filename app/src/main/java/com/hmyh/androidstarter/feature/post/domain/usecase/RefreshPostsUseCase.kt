package com.hmyh.androidstarter.feature.post.domain.usecase

import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.feature.post.domain.repository.PostRepository
import javax.inject.Inject

class RefreshPostsUseCase @Inject constructor(
    private val repository: PostRepository,
) {
    suspend operator fun invoke(): AppResult<Unit> = repository.refresh()
}
