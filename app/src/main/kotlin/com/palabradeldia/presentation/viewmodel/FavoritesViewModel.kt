package com.palabradeldia.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palabradeldia.domain.model.Word
import com.palabradeldia.domain.usecase.ObserveFavouritesUseCase
import com.palabradeldia.domain.usecase.ToggleFavouriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FavoritesUiState {
    data object Empty   : FavoritesUiState
    data class  List(val words: kotlin.collections.List<Word>) : FavoritesUiState
}

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    observeFavourites: ObserveFavouritesUseCase,
    private val toggleFavourite: ToggleFavouriteUseCase
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = observeFavourites()
        .map { words ->
            if (words.isEmpty()) FavoritesUiState.Empty
            else FavoritesUiState.List(words)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState.Empty)

    fun onRemove(word: Word) {
        viewModelScope.launch { toggleFavourite(word) }
    }
}
