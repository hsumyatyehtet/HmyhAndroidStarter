package com.hmyh.androidstarter.feature.post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hmyh.androidstarter.core.common.AppError
import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.feature.post.domain.usecase.ObservePostsUseCase
import com.hmyh.androidstarter.feature.post.domain.usecase.RefreshPostsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PostsViewModel @Inject constructor(
    observePosts: ObservePostsUseCase,
    private val refreshPosts: RefreshPostsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostsUiState())
    val uiState: StateFlow<PostsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observePosts().collect { posts ->
                _uiState.update { it.copy(items = posts) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, errorMessage = null) }
            when (val result = refreshPosts()) {
                is AppResult.Success -> _uiState.update { it.copy(isRefreshing = false) }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isRefreshing = false,
                        errorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }
}

private fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "Couldn't reach the server. Showing offline posts."
    AppError.Http -> "The server returned an error. Showing offline posts."
    is AppError.Unknown -> message
}
