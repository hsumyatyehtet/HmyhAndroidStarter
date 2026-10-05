package com.hmyh.androidstarter.feature.post.presentation

import com.hmyh.androidstarter.core.common.AppError
import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.feature.post.domain.model.Post
import com.hmyh.androidstarter.feature.post.domain.usecase.ObservePostsUseCase
import com.hmyh.androidstarter.feature.post.domain.usecase.RefreshPostsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class PostsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun refreshSuccess_exposesPostsAndFinishesRefreshing() {
        val viewModel = viewModel(
            posts = listOf(
                Post(id = 1, userId = 1, title = "Hello", body = "Body"),
                Post(id = 2, userId = 1, title = "World", body = "Body"),
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(listOf("Hello", "World"), state.items.map { it.title })
        assertFalse(state.isRefreshing)
        assertNull(state.errorMessage)
    }

    @Test
    fun refreshNetworkFailure_keepsCachedPosts() {
        val viewModel = viewModel(
            posts = listOf(Post(id = 1, userId = 1, title = "Cached", body = "Body")),
            refreshResult = AppResult.Failure(AppError.Network),
        )

        val state = viewModel.uiState.value
        assertEquals(listOf("Cached"), state.items.map { it.title })
        assertEquals("Couldn't reach the server. Showing offline posts.", state.errorMessage)
        assertFalse(state.isRefreshing)
    }

    private fun viewModel(
        posts: List<Post>,
        refreshResult: AppResult<Unit> = AppResult.Success(Unit),
    ): PostsViewModel {
        val repository = FakePostRepository(posts, refreshResult)
        return PostsViewModel(
            observePosts = ObservePostsUseCase(repository),
            refreshPosts = RefreshPostsUseCase(repository),
        )
    }
}
