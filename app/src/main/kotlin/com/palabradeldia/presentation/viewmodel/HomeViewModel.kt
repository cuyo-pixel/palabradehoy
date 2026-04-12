package com.palabradeldia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palabradeldia.domain.model.DailyWord
import com.palabradeldia.domain.usecase.GetTodayWordUseCase
import com.palabradeldia.domain.usecase.ObserveFavouritesUseCase
import com.palabradeldia.domain.usecase.ToggleFavouriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class  Success(val dailyWord: DailyWord, val isFavourite: Boolean) : HomeUiState
    data class  Error(val message: String) : HomeUiState
}

/**
 * ViewModel for HomeScreen.
 *
 * The favourite flag is derived by combining the daily word flow with the
 * live favourites flow. This guarantees the star icon on the home screen
 * always reflects the current database state, even when the word is removed
 * from the Favourites screen while the app is running.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getTodayWord: GetTodayWordUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase,
    private val observeFavourites: ObserveFavouritesUseCase
) : ViewModel() {

    // Holds the loaded daily word; null while loading or on error.
    private val _dailyWord = MutableStateFlow<DailyWord?>(null)
    private val _error     = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> =
        combine(_dailyWord, _error, observeFavourites()) { word, error, favourites ->
            when {
                error != null -> HomeUiState.Error(error)
                word  != null -> HomeUiState.Success(
                    dailyWord   = word,
                    isFavourite = favourites.any { it.id == word.word.id }
                )
                else -> HomeUiState.Loading
            }
        }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = HomeUiState.Loading
        )

    init {
        loadTodayWord()
    }

    fun onToggleFavourite() {
        val current = _dailyWord.value ?: return
        viewModelScope.launch {
            toggleFavourite(current.word)
        }
    }

    private fun loadTodayWord() {
        viewModelScope.launch {
            runCatching { getTodayWord() }
                .onSuccess { _dailyWord.value = it }
                .onFailure { _error.value = it.message ?: "Error desconocido" }
        }
    }
}
