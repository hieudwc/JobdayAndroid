package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpVerificationDto(
    val challengeId: String,
    val code: String,
    val deviceId: String? = null,
    val deviceName: String? = null,
)