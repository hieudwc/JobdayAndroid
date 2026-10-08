package vn.jobday.core.model.auth

data class OtpChallenge(
    val challengeId: String,
    val expiresInSeconds: Int,
    val retryAfterSeconds: Int,
    val debugOtp: String?,
)