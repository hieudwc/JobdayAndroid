package vn.jobday.core.data.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import vn.jobday.core.common.AppError
import vn.jobday.core.common.AppResult
import vn.jobday.core.data.auth.mapper.toDomain
import vn.jobday.core.data.auth.mapper.toDto
import vn.jobday.core.data.network.ApiCallExecutor
import vn.jobday.core.datastore.session.SessionStore
import vn.jobday.core.model.auth.AccountType
import vn.jobday.core.model.auth.AuthenticatedSession
import vn.jobday.core.model.auth.OtpChallenge
import vn.jobday.core.model.auth.User
import vn.jobday.core.network.auth.api.AuthApi
import vn.jobday.core.network.auth.dto.OtpRequestDto
import vn.jobday.core.network.auth.dto.OtpVerificationDto
import vn.jobday.core.network.auth.dto.DisplayNameUpdateDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultAuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val apiCallExecutor: ApiCallExecutor,
    private val sessionStore: SessionStore,
) : AuthRepository {

    override val session: Flow<AuthenticatedSession?> = sessionStore.session

    override suspend fun requestOtp(
        phoneNumber: String,
        accountType: AccountType,
    ): AppResult<OtpChallenge> {
        return apiCallExecutor.execute {
            authApi.requestOtp(
                request = OtpRequestDto(
                    phoneNumber = phoneNumber,
                    accountType = accountType.toDto(),
                ),
            ).toDomain()
        }
    }

    override suspend fun verifyOtp(
        challengeId: String,
        code: String,
        deviceId: String?,
        deviceName: String?,
    ): AppResult<AuthenticatedSession> {
        return apiCallExecutor.execute {
            authApi.verifyOtp(
                request = OtpVerificationDto(
                    challengeId = challengeId,
                    code = code,
                    deviceId = deviceId,
                    deviceName = deviceName,
                ),
            ).toDomain()
        }
    }

    override suspend fun saveSession(
        session: AuthenticatedSession,
    ): AppResult<Unit> {
        return try {
            sessionStore.saveSession(session)
            AppResult.Success(Unit)
        } catch (exception: Exception) {
            AppResult.Failure(
                AppError.Unknown(exception),
            )
        }
    }

    override suspend fun updateDisplayName(
        displayName: String,
    ): AppResult<User> {
        return apiCallExecutor.execute {
            val user = authApi.updateCurrentUser(
                DisplayNameUpdateDto(
                    displayName = displayName,
                ),
            ).toDomain()

            val currentSession = checkNotNull(
                sessionStore.session.first(),
            ) {
                "Cannot update a user without an authenticated session."
            }

            sessionStore.saveSession(
                currentSession.copy(user = user),
            )

            user
        }
    }

    override suspend fun clearLocalSession() {
        sessionStore.clearSession()
    }
}
