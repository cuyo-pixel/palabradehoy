package com.palabradeldia.domain.model

data class Word(
    val id: Int,
    val word: String,
    val pos: String,
    val gender: String?,
    val etymology: String?,
    val definitions: List<Definition>
)

data class Definition(
    val number: Int,
    val text: String,
    val example: String? = null
)
