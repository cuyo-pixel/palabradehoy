package com.cuyo_pixel.palabra_de_hoy.domain.model

import java.time.LocalDate

data class DailyWord(
    val date: LocalDate,
    val word: Word
)

enum class ThemeMode { SYSTEM, LIGHT, DARK, OLED }
