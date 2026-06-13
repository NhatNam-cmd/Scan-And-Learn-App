package com.example.englishapp.core.common

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()

    data class Error(
        val message: String,
        val exception: Throwable? = null
    ) : ApiResult<Nothing>()

    object Loading : ApiResult<Nothing>()

    data class Fallback<out T>(
        val data: T,
        val reason: String = "AI Quota Exceeded or Network Error"
    ) : ApiResult<T>()
}