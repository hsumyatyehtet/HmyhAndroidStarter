package com.hmyh.androidstarter.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val value: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

sealed interface AppError {
    data object Network : AppError
    data object Http : AppError
    data class Unknown(val message: String) : AppError
}
