package vn.jobday.core.network.error

import kotlinx.serialization.Serializable

@Serializable
data class ProblemDetailsDto(
    val type: String,
    val title: String,
    val status: Int,
    val code: String,
    val detail: String,
    val instance: String? = null,
    val violations: List<ViolationDto> = emptyList(),
)

@Serializable
data class ViolationDto(
    val field: String? = null,
    val message: String? = null,
)