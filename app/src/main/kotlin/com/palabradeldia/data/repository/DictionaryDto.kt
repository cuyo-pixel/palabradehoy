package com.palabradeldia.data.repository

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// DTOs matching the structure of assets/dictionary.json.

@Serializable
internal data class WordDto(
    @SerialName("id")          val id: Int,
    @SerialName("word")        val word: String,
    @SerialName("pos")         val pos: String,
    @SerialName("gender")      val gender: String?      = null,
    @SerialName("etymology")   val etymology: String?   = null,
    @SerialName("definitions") val definitions: List<DefinitionDto> = emptyList()
)

@Serializable
internal data class DefinitionDto(
    @SerialName("n")       val number: Int,
    @SerialName("text")    val text: String,
    @SerialName("example") val example: String? = null
)
