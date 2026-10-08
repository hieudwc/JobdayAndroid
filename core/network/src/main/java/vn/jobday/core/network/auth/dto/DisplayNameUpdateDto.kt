package vn.jobday.core.network.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class DisplayNameUpdateDto(
    val displayName: String,
)
