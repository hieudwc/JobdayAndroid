package vn.jobday.core.model.auth

data class AuthenticatedSession(
    val user: User,
    val tokens: AuthTokens,
)