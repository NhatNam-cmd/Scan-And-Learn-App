package com.example.englishapp.core.network.dictionary

import kotlinx.serialization.Serializable

@Serializable
data class DictionaryResponseDto(
    val word: String,
    val phonetic: String? = null,
    val meanings: List<MeaningDto> = emptyList()
)

@Serializable
data class MeaningDto(
    val partOfSpeech: String,
    val definitions: List<DefinitionDto> = emptyList()
)

@Serializable
data class DefinitionDto(
    val definition: String,
    val example: String? = null
)