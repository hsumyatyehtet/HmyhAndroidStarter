package com.hmyh.androidstarter.feature.post.data.local

import android.content.Context
import com.hmyh.androidstarter.feature.post.data.remote.PostDto
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostAssetDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    fun loadPosts(): List<PostDto> {
        val text = context.assets.open(FILE_NAME).bufferedReader().use { it.readText() }
        return json.decodeFromString(text)
    }

    private companion object {
        const val FILE_NAME = "posts.json"
    }
}
