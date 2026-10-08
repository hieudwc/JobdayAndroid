package vn.jobday.feature.auth.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.jobday.core.common.AppError
import vn.jobday.core.common.AppResult
import vn.jobday.core.data.auth.AuthRepository
import vn.jobday.core.model.auth.AccountType
import vn.jobday.feature.auth.ARG_ACCOUNT_TYPE
import vn.jobday.feature.auth.ARG_CHALLENGE_ID
import vn.jobday.feature.auth.ARG_DEBUG_OTP
import vn.jobday.feature.auth.ARG_PHONE_NUMBER
import vn.jobday.feature.auth.ARG_RETRY_AFTER_SECONDS
import vn.jobday.feature.auth.toAuthMessage
import javax.inject.Inject

data class OtpUiState(
    val phoneNumber: String,
    val code: String = "",
    val debugOtp: String? = null,
    val resendSeconds: Int = 0,
    val isSubmitting: Boolean = false,
    val isResending: Boolean = false,
    val isVerified: Boolean = false,
    val codeError: String? = null,
    val errorMessage: String? = null,
)

sealed interface OtpAction {

    data class CodeChanged(
        val value: String,
    ) : OtpAction

    data object Submit : OtpAction

    data object Resend : OtpAction
}

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var challengeId: String = checkNotNull(
        savedStateHandle[ARG_CHALLENGE_ID],
    )

    private val phoneNumber: String = checkNotNull(
        savedStateHandle[ARG_PHONE_NUMBER],
    )

    private val accountType: AccountType = runCatching {
        AccountType.valueOf(
            savedStateHandle[ARG_ACCOUNT_TYPE]
                ?: AccountType.CANDIDATE.name,
        )
    }.getOrDefault(AccountType.CANDIDATE)

    private val initialRetrySeconds: Int =
        savedStateHandle[ARG_RETRY_AFTER_SECONDS] ?: 0

    private val _uiState = MutableStateFlow(
        OtpUiState(
            phoneNumber = phoneNumber,
            debugOtp = savedStateHandle[ARG_DEBUG_OTP],
            resendSeconds = initialRetrySeconds,
        ),
    )

    val uiState: StateFlow<OtpUiState> =
        _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        startResendCountdown(initialRetrySeconds)
    }

    fun onAction(action: OtpAction) {
        when (action) {
            is OtpAction.CodeChanged -> {
                _uiState.update {
                    it.copy(
                        code = action.value
                            .filter(Char::isDigit)
                            .take(6),
                        codeError = null,
                        errorMessage = null,
                    )
                }
            }

            OtpAction.Submit -> verifyOtp()
            OtpAction.Resend -> resendOtp()
        }
    }

    private fun verifyOtp() {
        val state = _uiState.value

        if (state.isSubmitting || state.isResending) return

        if (state.code.length != 6) {
            _uiState.update {
                it.copy(
                    codeError = "Mã OTP phải gồm đúng 6 chữ số.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                isSubmitting = true,
                codeError = null,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (
                val result = authRepository.verifyOtp(
                    challengeId = challengeId,
                    code = _uiState.value.code,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isVerified = true,
                        )
                    }

                    delay(SUCCESS_DISPLAY_MILLIS)

                    when (
                        val saveResult = authRepository.saveSession(
                            result.value,
                        )
                    ) {
                        is AppResult.Success -> Unit

                        is AppResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isVerified = false,
                                    errorMessage =
                                        saveResult.error.toAuthMessage(),
                                )
                            }
                        }
                    }
                }

                is AppResult.Failure -> {
                    val isInvalidOtp =
                        result.error is AppError.Unauthorized

                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            codeError = if (isInvalidOtp) {
                                "Mã OTP không hợp lệ hoặc đã hết hạn."
                            } else {
                                null
                            },
                            errorMessage = if (isInvalidOtp) {
                                null
                            } else {
                                result.error.toAuthMessage()
                            },
                        )
                    }
                }
            }
        }
    }

    private fun resendOtp() {
        val state = _uiState.value

        if (
            state.resendSeconds > 0 ||
            state.isSubmitting ||
            state.isResending
        ) {
            return
        }

        _uiState.update {
            it.copy(
                isResending = true,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (
                val result = authRepository.requestOtp(
                    phoneNumber = phoneNumber,
                    accountType = accountType,
                )
            ) {
                is AppResult.Success -> {
                    challengeId = result.value.challengeId

                    _uiState.update {
                        it.copy(
                            code = "",
                            debugOtp = result.value.debugOtp,
                            isResending = false,
                            codeError = null,
                        )
                    }

                    startResendCountdown(
                        result.value.retryAfterSeconds,
                    )
                }

                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isResending = false,
                            errorMessage =
                                result.error.toAuthMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun startResendCountdown(seconds: Int) {
        countdownJob?.cancel()

        val initialSeconds = seconds.coerceAtLeast(0)

        _uiState.update {
            it.copy(resendSeconds = initialSeconds)
        }

        if (initialSeconds == 0) return

        countdownJob = viewModelScope.launch {
            var remaining = initialSeconds

            while (remaining > 0) {
                delay(1_000)
                remaining--

                _uiState.update {
                    it.copy(resendSeconds = remaining)
                }
            }
        }
    }

    private companion object {
        const val SUCCESS_DISPLAY_MILLIS = 1_200L
    }
}
