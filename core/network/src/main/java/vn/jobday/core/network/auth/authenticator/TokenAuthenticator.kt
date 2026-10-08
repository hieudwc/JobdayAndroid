package vn.jobday.core.network.auth.authenticator

import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException
import vn.jobday.core.datastore.session.SessionStore
import vn.jobday.core.network.auth.RefreshTokenApi
import vn.jobday.core.network.auth.SkipAuthenticationTag
import vn.jobday.core.network.auth.dto.RefreshRequestDto
import vn.jobday.core.network.auth.mapper.toAuthTokens
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val sessionStore: SessionStore,
    private val refreshTokenApi: RefreshTokenApi,
) : Authenticator {

    private val refreshLock = Any()

    override fun authenticate(
        route: Route?,
        response: Response,
    ): Request? {
        val isPublicRequest =
            response.request.tag(SkipAuthenticationTag::class.java) != null

        if (isPublicRequest) {
            return null
        }

        if (responseCount(response) >= MAX_AUTH_ATTEMPTS) {
            return null
        }

        return synchronized(refreshLock) {
            runBlocking {
                refreshAndRetry(response)
            }
        }
    }

    private suspend fun refreshAndRetry(
        response: Response,
    ): Request? {
        val failedAuthorization =
            response.request.header(AUTHORIZATION_HEADER)

        val latestAccessToken =
            sessionStore.getAccessToken()

        /*
         * Nếu access token trong SessionStore khác token của request bị 401,
         * nghĩa là request khác đã refresh thành công trong lúc ta chờ lock.
         * Không gọi refresh lần nữa, chỉ retry bằng token mới nhất.
         */
        if (
            !latestAccessToken.isNullOrBlank() &&
            failedAuthorization != bearerToken(latestAccessToken)
        ) {
            return response.request.withAccessToken(latestAccessToken)
        }

        val refreshToken =
            sessionStore.getRefreshToken()
                ?: return null

        return try {
            val tokenResponse = refreshTokenApi.refreshToken(
                request = RefreshRequestDto(
                    refreshToken = refreshToken,
                ),
            )

            val newTokens = tokenResponse.toAuthTokens()

            sessionStore.updateTokens(newTokens)

            response.request.withAccessToken(
                accessToken = newTokens.accessToken,
            )
        } catch (exception: HttpException) {
            /*
             * 400/401 từ endpoint refresh nghĩa là refresh token không còn
             * hợp lệ. Xoá session để ứng dụng chuyển người dùng về đăng nhập.
             */
            if (exception.code() == 400 || exception.code() == 401) {
                sessionStore.clearSession()
            }

            null
        } catch (exception: IOException) {
            /*
             * Mất mạng hoặc timeout không đồng nghĩa refresh token hết hạn.
             * Không xoá session.
             */
            null
        } catch (exception: Exception) {
            null
        }
    }

    private fun Request.withAccessToken(
        accessToken: String,
    ): Request {
        return newBuilder()
            .header(
                AUTHORIZATION_HEADER,
                bearerToken(accessToken),
            )
            .build()
    }

    private fun bearerToken(
        accessToken: String,
    ): String {
        return "$BEARER_PREFIX $accessToken"
    }

    private fun responseCount(
        response: Response,
    ): Int {
        var count = 1
        var currentResponse = response.priorResponse

        while (currentResponse != null) {
            count++
            currentResponse = currentResponse.priorResponse
        }

        return count
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"

        // Request ban đầu + tối đa một lần retry.
        const val MAX_AUTH_ATTEMPTS = 2
    }
}