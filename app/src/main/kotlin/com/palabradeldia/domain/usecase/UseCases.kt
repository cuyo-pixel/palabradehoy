package com.palabradeldia.domain.usecase

import com.palabradeldia.domain.model.DailyWord
import com.palabradeldia.domain.model.ThemeMode
import com.palabradeldia.domain.model.Word
import com.palabradeldia.domain.repository.WordRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetTodayWordUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(): DailyWord = repository.getTodayWord()
}

class GetWordForDateUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(date: LocalDate): DailyWord = repository.getWordForDate(date)
}

class ObserveFavouritesUseCase @Inject constructor(private val repository: WordRepository) {
    operator fun invoke(): Flow<List<Word>> = repository.getFavourites()
}

class ToggleFavouriteUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(word: Word): Boolean {
        return if (repository.isFavourite(word.id)) {
            repository.removeFavourite(word); false
        } else {
            repository.addFavourite(word); true
        }
    }
}

class ObserveThemeModeUseCase @Inject constructor(private val repository: WordRepository) {
    operator fun invoke(): Flow<ThemeMode> = repository.getThemeMode()
}

class SetThemeModeUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(mode: ThemeMode) = repository.setThemeMode(mode)
}

class ExportFavouritesUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(): String = repository.exportFavourites()
}

class ImportFavouritesUseCase @Inject constructor(private val repository: WordRepository) {
    suspend operator fun invoke(json: String): Int = repository.importFavourites(json)
}
