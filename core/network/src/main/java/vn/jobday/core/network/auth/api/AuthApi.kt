package vn.jobday.core.network.auth.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PATCH
import retrofit2.http.POST
import vn.jobday.core.network.auth.SKIP_AUTH_HEADER_LINE
import vn.jobday.core.network.auth.dto.OtpChallengeDto
import vn.jobday.core.network.auth.dto.DisplayNameUpdateDto
import vn.jobday.core.network.auth.dto.OtpRequestDto
import vn.jobday.core.network.auth.dto.OtpVerificationDto
import vn.jobday.core.network.auth.dto.RefreshRequestDto
import vn.jobday.core.network.auth.dto.TokenResponseDto
import vn.jobday.core.network.auth.dto.UserDto


interface AuthApi {
    @Headers(SKIP_AUTH_HEADER_LINE)
    @POST("auth/otp/request")
    suspend fun requestOtp(
        @Body request: OtpRequestDto,
    ): OtpChallengeDto
    @Headers(SKIP_AUTH_HEADER_LINE)
    @POST("auth/otp/verify")
    suspend fun verifyOtp(
        @Body request: OtpVerificationDto,
    ): TokenResponseDto
    @Headers(SKIP_AUTH_HEADER_LINE)
    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshRequestDto,
    )

    @GET("users/me")
    suspend fun getCurrentUser(): UserDto

    @PATCH("users/me")
    suspend fun updateCurrentUser(
        @Body request: DisplayNameUpdateDto,
    ): UserDto
}
