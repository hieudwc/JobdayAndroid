package vn.jobday.core.network.auth

import retrofit2.http.Body
import retrofit2.http.POST
import vn.jobday.core.network.auth.dto.RefreshRequestDto
import vn.jobday.core.network.auth.dto.TokenResponseDto

interface RefreshTokenApi {

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequestDto,
    ): TokenResponseDto
}