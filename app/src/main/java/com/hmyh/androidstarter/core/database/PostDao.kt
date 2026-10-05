package com.hmyh.androidstarter.core.database

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id ASC")
    fun observeAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) FROM posts")
    suspend fun count(): Int

    @Upsert
    suspend fun upsertAll(posts: List<PostEntity>)
}
