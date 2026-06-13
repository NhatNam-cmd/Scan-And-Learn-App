package com.project.englishapp.core.network.dictionary

import retrofit2.http.GET
import retrofit2.http.Path

interface DictionaryService {
    @GET("{word}")
    suspend fun lookupWord(@Path("word") word: String): List<DictionaryResponseDto>
}