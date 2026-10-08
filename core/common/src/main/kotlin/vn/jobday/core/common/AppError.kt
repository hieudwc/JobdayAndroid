package vn.jobday.core.common

sealed interface AppError {

    data object NetworkUnavailable : AppError

    data object Unauthorized : AppError

    data class Validation(
        val code: String?,
        val message: String,
    ) : AppError

    data class Server(
        val status: Int,
        val code: String?,
        val message: String?,
    ) : AppError

    data class Unknown(
        val cause: Throwable? = null,
    ) : AppError
}