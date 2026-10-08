package vn.jobday.core.model.auth

data class User(
    val id: String,
    val phoneNumber: String,
    val accountType: AccountType,
    val displayName: String?,
    val avatarUrl: String?,
)