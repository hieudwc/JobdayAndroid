package vn.jobday.core.datastore.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import vn.jobday.core.datastore.crypto.TokenCipher
import vn.jobday.core.model.auth.AccountType
import vn.jobday.core.model.auth.AuthTokens
import vn.jobday.core.model.auth.AuthenticatedSession
import vn.jobday.core.model.auth.User
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "secure_session",
)

@Singleton
class DefaultSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenCipher: TokenCipher,
) : SessionStore {

    override val session: Flow<AuthenticatedSession?> =
        context.sessionDataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
                preferences.toAuthenticatedSession()
            }

    override suspend fun saveSession(
        session: AuthenticatedSession,
    ) {
        val encryptedAccessToken = tokenCipher.encrypt(
            session.tokens.accessToken,
        )

        val encryptedRefreshToken = tokenCipher.encrypt(
            session.tokens.refreshToken,
        )

        context.sessionDataStore.edit { preferences ->
            preferences[Keys.USER_ID] = session.user.id
            preferences[Keys.PHONE_NUMBER] = session.user.phoneNumber
            preferences[Keys.ACCOUNT_TYPE] = session.user.accountType.name

            session.user.displayName?.let { displayName ->
                preferences[Keys.DISPLAY_NAME] = displayName
            } ?: preferences.remove(Keys.DISPLAY_NAME)

            session.user.avatarUrl?.let { avatarUrl ->
                preferences[Keys.AVATAR_URL] = avatarUrl
            } ?: preferences.remove(Keys.AVATAR_URL)

            preferences[Keys.ACCESS_TOKEN] = encryptedAccessToken
            preferences[Keys.ACCESS_TOKEN_EXPIRES_AT] =
                session.tokens.accessTokenExpiresAtEpochSeconds

            preferences[Keys.REFRESH_TOKEN] = encryptedRefreshToken
            preferences[Keys.REFRESH_TOKEN_EXPIRES_AT] =
                session.tokens.refreshTokenExpiresAtEpochSeconds
        }
    }

    override suspend fun updateTokens(
        tokens: AuthTokens,
    ) {
        val encryptedAccessToken = tokenCipher.encrypt(
            tokens.accessToken,
        )

        val encryptedRefreshToken = tokenCipher.encrypt(
            tokens.refreshToken,
        )

        context.sessionDataStore.edit { preferences ->
            preferences[Keys.ACCESS_TOKEN] = encryptedAccessToken
            preferences[Keys.ACCESS_TOKEN_EXPIRES_AT] =
                tokens.accessTokenExpiresAtEpochSeconds

            preferences[Keys.REFRESH_TOKEN] = encryptedRefreshToken
            preferences[Keys.REFRESH_TOKEN_EXPIRES_AT] =
                tokens.refreshTokenExpiresAtEpochSeconds
        }
    }

    override suspend fun getAccessToken(): String? {
        val encryptedToken = context.sessionDataStore.data
            .first()[Keys.ACCESS_TOKEN]
            ?: return null

        return runCatching {
            tokenCipher.decrypt(encryptedToken)
        }.getOrNull()
    }

    override suspend fun getRefreshToken(): String? {
        val encryptedToken = context.sessionDataStore.data
            .first()[Keys.REFRESH_TOKEN]
            ?: return null

        return runCatching {
            tokenCipher.decrypt(encryptedToken)
        }.getOrNull()
    }

    override suspend fun clearSession() {
        context.sessionDataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private fun Preferences.toAuthenticatedSession(): AuthenticatedSession? {
        val userId = this[Keys.USER_ID]
            ?: return null

        val phoneNumber = this[Keys.PHONE_NUMBER]
            ?: return null

        val accountTypeName = this[Keys.ACCOUNT_TYPE]
            ?: return null

        val accountType = runCatching {
            AccountType.valueOf(accountTypeName)
        }.getOrNull()
            ?: return null

        val encryptedAccessToken = this[Keys.ACCESS_TOKEN]
            ?: return null

        val accessToken = runCatching {
            tokenCipher.decrypt(encryptedAccessToken)
        }.getOrNull()
            ?: return null

        val accessTokenExpiresAt = this[Keys.ACCESS_TOKEN_EXPIRES_AT]
            ?: return null

        val encryptedRefreshToken = this[Keys.REFRESH_TOKEN]
            ?: return null

        val refreshToken = runCatching {
            tokenCipher.decrypt(encryptedRefreshToken)
        }.getOrNull()
            ?: return null

        val refreshTokenExpiresAt = this[Keys.REFRESH_TOKEN_EXPIRES_AT]
            ?: return null

        return AuthenticatedSession(
            user = User(
                id = userId,
                phoneNumber = phoneNumber,
                accountType = accountType,
                displayName = this[Keys.DISPLAY_NAME],
                avatarUrl = this[Keys.AVATAR_URL],
            ),
            tokens = AuthTokens(
                accessToken = accessToken,
                accessTokenExpiresAtEpochSeconds = accessTokenExpiresAt,
                refreshToken = refreshToken,
                refreshTokenExpiresAtEpochSeconds = refreshTokenExpiresAt,
            ),
        )
    }

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val PHONE_NUMBER = stringPreferencesKey("phone_number")
        val ACCOUNT_TYPE = stringPreferencesKey("account_type")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_URL = stringPreferencesKey("avatar_url")

        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val ACCESS_TOKEN_EXPIRES_AT =
            longPreferencesKey("access_token_expires_at")

        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val REFRESH_TOKEN_EXPIRES_AT =
            longPreferencesKey("refresh_token_expires_at")
    }
}