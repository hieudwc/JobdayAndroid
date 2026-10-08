package vn.jobday.core.data.auth

import kotlinx.coroutines.flow.Flow
import vn.jobday.core.common.AppResult
import vn.jobday.core.model.auth.AccountType
import vn.jobday.core.model.auth.AuthenticatedSession
import vn.jobday.core.model.auth.OtpChallenge
import vn.jobday.core.model.auth.User

interface AuthRepository {

    val session: Flow<AuthenticatedSession?>

    suspend fun requestOtp(
        phoneNumber: String,
        accountType: AccountType,
    ): AppResult<OtpChallenge>

    suspend fun verifyOtp(
        challengeId: String,
        code: String,
        deviceId: String? = null,
        deviceName: String? = null,
    ): AppResult<AuthenticatedSession>

    suspend fun saveSession(
        session: AuthenticatedSession,
    ): AppResult<Unit>

    suspend fun updateDisplayName(
        displayName: String,
    ): AppResult<User>

    suspend fun clearLocalSession()
}
