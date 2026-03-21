package com.palabradeldia.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.palabradeldia.data.db.AppDatabase
import com.palabradeldia.data.repository.WordRepositoryImpl
import com.palabradeldia.domain.repository.WordRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.preferencesDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "palabradeldia.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> =
        ctx.preferencesDataStore
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindWordRepository(impl: WordRepositoryImpl): WordRepository
}
