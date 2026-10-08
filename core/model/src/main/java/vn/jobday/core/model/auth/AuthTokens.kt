package vn.jobday.core.model.auth

data class AuthTokens(
    val accessToken: String,
    val accessTokenExpiresAtEpochSeconds: Long,
    val refreshToken: String,
    val refreshTokenExpiresAtEpochSeconds: Long,
) {
    override fun toString(): String {
        return "AuthTokens(accessToken=***, refreshToken=***)"
    }
}