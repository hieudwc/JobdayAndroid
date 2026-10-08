package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpChallengeDto(
    val challengeId: String,
    val expiresInSeconds: Int,
    val retryAfterSeconds: Int,
    val debugOtp: String? = null,
)