package vn.jobday.core.network.auth.interceptor

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import vn.jobday.core.datastore.session.SessionStore
import vn.jobday.core.network.auth.SKIP_AUTH_HEADER
import vn.jobday.core.network.auth.SkipAuthenticationTag
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccessTokenInterceptor @Inject constructor(
    private val sessionStore: SessionStore,
) : Interceptor {

    override fun intercept(
        chain: Interceptor.Chain,
    ): Response {
        val originalRequest = chain.request()

        val shouldSkipAuthentication =
            originalRequest.header(SKIP_AUTH_HEADER) != null

        if (shouldSkipAuthentication) {
            val cleanRequest = originalRequest.newBuilder()
                .removeHeader(SKIP_AUTH_HEADER)
                .tag(
                    SkipAuthenticationTag::class.java,
                    SkipAuthenticationTag,
                )
                .build()

            return chain.proceed(cleanRequest)
        }

        if (originalRequest.header(AUTHORIZATION_HEADER) != null) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking {
            sessionStore.getAccessToken()
        }

        if (accessToken.isNullOrBlank()) {
            return chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header(
                AUTHORIZATION_HEADER,
                "$BEARER_PREFIX $accessToken",
            )
            .build()

        return chain.proceed(authenticatedRequest)
    }

    private companion object {
        const val AUTHORIZATION_HEADER = "Authorization"
        const val BEARER_PREFIX = "Bearer"
    }
}