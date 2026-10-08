package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val phoneNumber: String,
    val accountType: AccountTypeDto,
    val displayName: String? = null,
    val avatarUrl: String? = null,
)