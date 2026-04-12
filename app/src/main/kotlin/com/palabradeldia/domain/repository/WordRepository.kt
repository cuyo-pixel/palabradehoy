package com.palabradeldia.domain.repository

import com.palabradeldia.domain.model.DailyWord
import com.palabradeldia.domain.model.ThemeMode
import com.palabradeldia.domain.model.Word
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface WordRepository {

    suspend fun getDictionarySize(): Int
    suspend fun getWordForDate(date: LocalDate): DailyWord
    suspend fun getTodayWord(): DailyWord

    fun getFavourites(): Flow<List<Word>>
    suspend fun addFavourite(word: Word)
    suspend fun removeFavourite(word: Word)
    suspend fun isFavourite(wordId: Int): Boolean
    suspend fun exportFavourites(): String
    suspend fun importFavourites(json: String): Int

    fun getThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
