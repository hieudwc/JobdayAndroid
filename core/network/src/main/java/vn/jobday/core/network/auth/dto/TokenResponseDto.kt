package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponseDto(
    val tokenType: String,
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
    val refreshTokenExpiresAt: String,
    val user: UserDto,
)