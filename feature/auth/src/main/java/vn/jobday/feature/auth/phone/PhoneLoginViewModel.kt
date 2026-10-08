package vn.jobday.feature.auth.phone

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.jobday.core.common.AppResult
import vn.jobday.core.data.auth.AuthRepository
import vn.jobday.core.model.auth.AccountType
import vn.jobday.core.model.auth.OtpChallenge
import vn.jobday.feature.auth.ARG_ACCOUNT_TYPE
import vn.jobday.feature.auth.toAuthMessage
import javax.inject.Inject

data class PhoneLoginUiState(
    val phoneNumber: String = "",
    val accountType: AccountType = AccountType.CANDIDATE,
    val isLoading: Boolean = false,
    val phoneError: String? = null,
    val errorMessage: String? = null,
)

sealed interface PhoneLoginAction {

    data class PhoneChanged(
        val value: String,
    ) : PhoneLoginAction

    data class AccountTypeChanged(
        val value: AccountType,
    ) : PhoneLoginAction

    data object Submit : PhoneLoginAction
}

sealed interface PhoneLoginEffect {

    data class NavigateToOtp(
        val phoneNumber: String,
        val accountType: AccountType,
        val challenge: OtpChallenge,
    ) : PhoneLoginEffect
}

@HiltViewModel
class PhoneLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val initialAccountType = runCatching {
        AccountType.valueOf(
            savedStateHandle[ARG_ACCOUNT_TYPE]
                ?: AccountType.CANDIDATE.name,
        )
    }.getOrDefault(AccountType.CANDIDATE)

    private val _uiState = MutableStateFlow(
        PhoneLoginUiState(
            accountType = initialAccountType,
        ),
    )

    val uiState: StateFlow<PhoneLoginUiState> =
        _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<PhoneLoginEffect>()

    val effect: SharedFlow<PhoneLoginEffect> =
        _effect.asSharedFlow()

    fun onAction(action: PhoneLoginAction) {
        when (action) {
            is PhoneLoginAction.PhoneChanged -> {
                _uiState.update {
                    it.copy(
                        phoneNumber = action.value,
                        phoneError = null,
                        errorMessage = null,
                    )
                }
            }

            is PhoneLoginAction.AccountTypeChanged -> {
                _uiState.update {
                    it.copy(
                        accountType = action.value,
                        errorMessage = null,
                    )
                }
            }

            PhoneLoginAction.Submit -> requestOtp()
        }
    }

    private fun requestOtp() {
        if (_uiState.value.isLoading) return

        val normalizedPhone = normalizePhoneNumber(
            _uiState.value.phoneNumber,
        )

        if (!E164_REGEX.matches(normalizedPhone)) {
            _uiState.update {
                it.copy(
                    phoneError =
                        "Số điện thoại không hợp lệ. Ví dụ: 0901234567",
                )
            }
            return
        }

        val accountType = _uiState.value.accountType

        _uiState.update {
            it.copy(
                phoneNumber = normalizedPhone,
                isLoading = true,
                phoneError = null,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (
                val result = authRepository.requestOtp(
                    phoneNumber = normalizedPhone,
                    accountType = accountType,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false)
                    }

                    _effect.emit(
                        PhoneLoginEffect.NavigateToOtp(
                            phoneNumber = normalizedPhone,
                            accountType = accountType,
                            challenge = result.value,
                        ),
                    )
                }

                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.error.toAuthMessage(),
                        )
                    }
                }
            }
        }
    }

    private fun normalizePhoneNumber(
        rawPhoneNumber: String,
    ): String {
        val compact = rawPhoneNumber
            .trim()
            .replace(" ", "")
            .replace("-", "")
            .replace(".", "")

        return when {
            compact.startsWith("+") -> compact
            compact.startsWith("0") -> "+84${compact.drop(1)}"
            compact.startsWith("84") -> "+$compact"
            else -> compact
        }
    }

    private companion object {
        val E164_REGEX = Regex("^\\+[1-9]\\d{7,14}$")
    }
}
