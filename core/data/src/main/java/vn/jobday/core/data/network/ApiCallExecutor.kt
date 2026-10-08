package vn.jobday.core.data.network

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import vn.jobday.core.common.AppError
import vn.jobday.core.common.AppResult
import vn.jobday.core.network.error.ProblemDetailsDto
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiCallExecutor @Inject constructor(
    private val json: Json,
) {

    suspend fun <T> execute(
        block: suspend () -> T,
    ): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (exception: IOException) {
            AppResult.Failure(
                AppError.NetworkUnavailable,
            )
        } catch (exception: HttpException) {
            AppResult.Failure(
                exception.toAppError(),
            )
        } catch (exception: Exception) {
            AppResult.Failure(
                AppError.Unknown(exception),
            )
        }
    }

    private fun HttpException.toAppError(): AppError {
        val problem = response()
            ?.errorBody()
            ?.string()
            ?.let(::decodeProblemDetails)

        return when {
            code() == 401 -> {
                AppError.Unauthorized
            }

            problem?.violations?.isNotEmpty() == true -> {
                AppError.Validation(
                    code = problem.code,
                    message = problem.detail,
                )
            }

            else -> {
                AppError.Server(
                    status = code(),
                    code = problem?.code,
                    message = problem?.detail ?: message(),
                )
            }
        }
    }

    private fun decodeProblemDetails(
        body: String,
    ): ProblemDetailsDto? {
        return runCatching {
            json.decodeFromString<ProblemDetailsDto>(body)
        }.getOrNull()
    }
}