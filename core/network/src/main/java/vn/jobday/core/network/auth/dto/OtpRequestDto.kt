package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class OtpRequestDto(
    val phoneNumber: String,
    val accountType: AccountTypeDto,
)