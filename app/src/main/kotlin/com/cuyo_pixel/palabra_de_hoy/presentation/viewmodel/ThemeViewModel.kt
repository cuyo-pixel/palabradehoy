package com.cuyo_pixel.palabra_de_hoy.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cuyo_pixel.palabra_de_hoy.domain.model.ThemeMode
import com.cuyo_pixel.palabra_de_hoy.domain.usecase.ObserveThemeModeUseCase
import com.cuyo_pixel.palabra_de_hoy.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(
    observeThemeMode: ObserveThemeModeUseCase,
    private val setThemeMode: SetThemeModeUseCase
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = observeThemeMode()
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.SYSTEM)

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch { setThemeMode(mode) }
    }
}
