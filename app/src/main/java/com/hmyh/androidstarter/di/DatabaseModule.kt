package com.hmyh.androidstarter.di

import android.content.Context
import androidx.room.Room
import com.hmyh.androidstarter.core.database.AppDatabase
import com.hmyh.androidstarter.core.database.PostDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "starter.db",
    )
        // Starter only. Replace with real migrations before the app stores user data.
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    @Provides
    fun providePostDao(database: AppDatabase): PostDao = database.postDao()
}
