package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
enum class AccountTypeDto {
    CANDIDATE,
    EMPLOYER,
    ADMIN,
}