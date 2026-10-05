package com.hmyh.androidstarter.feature.post.data.repository

import com.hmyh.androidstarter.core.common.AppError
import com.hmyh.androidstarter.core.common.AppResult
import com.hmyh.androidstarter.core.database.PostDao
import com.hmyh.androidstarter.di.IoDispatcher
import com.hmyh.androidstarter.feature.post.data.local.PostAssetDataSource
import com.hmyh.androidstarter.feature.post.data.mapper.toDomain
import com.hmyh.androidstarter.feature.post.data.mapper.toEntity
import com.hmyh.androidstarter.feature.post.data.remote.PostApi
import com.hmyh.androidstarter.feature.post.domain.model.Post
import com.hmyh.androidstarter.feature.post.domain.repository.PostRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val api: PostApi,
    private val dao: PostDao,
    private val assetDataSource: PostAssetDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : PostRepository {
    override fun observePosts(): Flow<List<Post>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refresh(): AppResult<Unit> = withContext(ioDispatcher) {
        try {
            dao.upsertAll(api.getPosts().map { it.toEntity() })
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: IOException) {
            seedFromAssetsIfEmpty()
            AppResult.Failure(AppError.Network)
        } catch (_: HttpException) {
            seedFromAssetsIfEmpty()
            AppResult.Failure(AppError.Http)
        } catch (error: Exception) {
            AppResult.Failure(AppError.Unknown(error.message ?: "Unknown error"))
        }
    }

    private suspend fun seedFromAssetsIfEmpty() {
        if (dao.count() == 0) {
            dao.upsertAll(assetDataSource.loadPosts().map { it.toEntity() })
        }
    }
}
