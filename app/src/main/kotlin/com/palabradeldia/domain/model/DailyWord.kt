package com.palabradeldia.domain.model

import java.time.LocalDate

data class DailyWord(
    val date: LocalDate,
    val word: Word
)

enum class ThemeMode { SYSTEM, LIGHT, DARK, OLED }
