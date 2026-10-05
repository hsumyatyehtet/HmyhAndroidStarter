package com.hmyh.androidstarter.feature.post.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hmyh.androidstarter.feature.post.domain.model.Post
import com.hmyh.androidstarter.ui.theme.AppTheme

@Composable
fun PostsRoute(viewModel: PostsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PostsScreen(state = state, onRetry = viewModel::refresh)
}

@Composable
fun PostsScreen(state: PostsUiState, onRetry: () -> Unit) {
    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.items.isEmpty() && state.isRefreshing -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                state.items.isEmpty() && state.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(text = state.errorMessage)
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }

                else -> {
                    Column(Modifier.fillMaxSize()) {
                        state.errorMessage?.let { message ->
                            Text(
                                text = message,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(items = state.items, key = { post -> post.id }) { post ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        text = post.title,
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(
                                        text = post.body,
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PostsScreenPreview() {
    AppTheme {
        PostsScreen(
            state = PostsUiState(
                items = listOf(
                    Post(id = 1, userId = 1, title = "Hello", body = "First cached post"),
                    Post(id = 2, userId = 1, title = "World", body = "Second cached post"),
                ),
            ),
            onRetry = {},
        )
    }
}
