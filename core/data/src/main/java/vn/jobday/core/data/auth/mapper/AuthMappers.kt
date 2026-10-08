package vn.jobday.core.data.auth.mapper

import kotlinx.datetime.Instant
import vn.jobday.core.model.auth.AccountType
import vn.jobday.core.model.auth.AuthTokens
import vn.jobday.core.model.auth.AuthenticatedSession
import vn.jobday.core.model.auth.OtpChallenge
import vn.jobday.core.model.auth.User
import vn.jobday.core.network.auth.dto.AccountTypeDto
import vn.jobday.core.network.auth.dto.OtpChallengeDto
import vn.jobday.core.network.auth.dto.TokenResponseDto
import vn.jobday.core.network.auth.dto.UserDto

internal fun AccountType.toDto(): AccountTypeDto {
    return when (this) {
        AccountType.CANDIDATE -> AccountTypeDto.CANDIDATE
        AccountType.EMPLOYER -> AccountTypeDto.EMPLOYER
        AccountType.ADMIN -> AccountTypeDto.ADMIN
    }
}

internal fun AccountTypeDto.toDomain(): AccountType {
    return when (this) {
        AccountTypeDto.CANDIDATE -> AccountType.CANDIDATE
        AccountTypeDto.EMPLOYER -> AccountType.EMPLOYER
        AccountTypeDto.ADMIN -> AccountType.ADMIN
    }
}

internal fun OtpChallengeDto.toDomain(): OtpChallenge {
    return OtpChallenge(
        challengeId = challengeId,
        expiresInSeconds = expiresInSeconds,
        retryAfterSeconds = retryAfterSeconds,
        debugOtp = debugOtp,
    )
}

internal fun UserDto.toDomain(): User {
    return User(
        id = id,
        phoneNumber = phoneNumber,
        accountType = accountType.toDomain(),
        displayName = displayName,
        avatarUrl = avatarUrl,
    )
}

internal fun TokenResponseDto.toDomain(): AuthenticatedSession {
    return AuthenticatedSession(
        user = user.toDomain(),
        tokens = AuthTokens(
            accessToken = accessToken,
            accessTokenExpiresAtEpochSeconds =
                Instant.parse(accessTokenExpiresAt).epochSeconds,
            refreshToken = refreshToken,
            refreshTokenExpiresAtEpochSeconds =
                Instant.parse(refreshTokenExpiresAt).epochSeconds,
        ),
    )
}