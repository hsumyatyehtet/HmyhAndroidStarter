package com.hmyh.androidstarter.feature.post.data.mapper

import com.hmyh.androidstarter.core.database.PostEntity
import com.hmyh.androidstarter.feature.post.data.remote.PostDto
import com.hmyh.androidstarter.feature.post.domain.model.Post

fun PostDto.toEntity(): PostEntity = PostEntity(
    id = id,
    userId = userId,
    title = title,
    body = body,
)

fun PostEntity.toDomain(): Post = Post(
    id = id,
    userId = userId,
    title = title,
    body = body,
)
