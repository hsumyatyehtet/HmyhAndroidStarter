package com.hmyh.androidstarter.feature.post.di

import com.hmyh.androidstarter.feature.post.data.remote.PostApi
import com.hmyh.androidstarter.feature.post.data.repository.PostRepositoryImpl
import com.hmyh.androidstarter.feature.post.domain.repository.PostRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PostApiModule {
    @Provides
    @Singleton
    fun providePostApi(retrofit: Retrofit): PostApi = retrofit.create(PostApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindPostRepository(impl: PostRepositoryImpl): PostRepository
}
