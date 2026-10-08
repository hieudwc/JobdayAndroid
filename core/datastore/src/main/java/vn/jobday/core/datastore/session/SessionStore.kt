package vn.jobday.core.datastore.session

import kotlinx.coroutines.flow.Flow
import vn.jobday.core.model.auth.AuthTokens
import vn.jobday.core.model.auth.AuthenticatedSession

interface SessionStore {

    val session: Flow<AuthenticatedSession?>

    suspend fun saveSession(
        session: AuthenticatedSession,
    )

    suspend fun updateTokens(
        tokens: AuthTokens,
    )

    suspend fun getAccessToken(): String?

    suspend fun getRefreshToken(): String?

    suspend fun clearSession()
}