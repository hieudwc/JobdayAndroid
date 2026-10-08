package vn.jobday.core.network.auth.mapper

import kotlinx.datetime.Instant
import vn.jobday.core.model.auth.AuthTokens
import vn.jobday.core.network.auth.dto.TokenResponseDto

internal fun TokenResponseDto.toAuthTokens(): AuthTokens {
    return AuthTokens(
        accessToken = accessToken,
        accessTokenExpiresAtEpochSeconds =
            Instant.parse(accessTokenExpiresAt).epochSeconds,
        refreshToken = refreshToken,
        refreshTokenExpiresAtEpochSeconds =
            Instant.parse(refreshTokenExpiresAt).epochSeconds,
    )
}