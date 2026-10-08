package vn.jobday.feature.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import vn.jobday.core.common.AppResult
import vn.jobday.core.data.auth.AuthRepository
import vn.jobday.feature.auth.toAuthMessage
import javax.inject.Inject

data class CandidateNameUiState(
    val name: String = "",
    val isLoading: Boolean = false,
    val isCompleted: Boolean = false,
    val nameError: String? = null,
    val errorMessage: String? = null,
)

sealed interface CandidateNameAction {
    data class NameChanged(val value: String) : CandidateNameAction
    data object Submit : CandidateNameAction
}

@HiltViewModel
class CandidateNameViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CandidateNameUiState())
    val uiState: StateFlow<CandidateNameUiState> = _uiState.asStateFlow()

    fun onAction(action: CandidateNameAction) {
        when (action) {
            is CandidateNameAction.NameChanged -> {
                _uiState.update {
                    it.copy(
                        name = action.value.take(MAX_NAME_LENGTH),
                        nameError = null,
                        errorMessage = null,
                    )
                }
            }

            CandidateNameAction.Submit -> submitName()
        }
    }

    private fun submitName() {
        val state = _uiState.value
        if (state.isLoading || state.isCompleted) return

        val normalizedName = state.name
            .trim()
            .replace(WHITESPACE_REGEX, " ")

        if (normalizedName.length < MIN_NAME_LENGTH) {
            _uiState.update {
                it.copy(
                    nameError = "Vui lòng nhập họ và tên của bạn.",
                )
            }
            return
        }

        _uiState.update {
            it.copy(
                name = normalizedName,
                isLoading = true,
                nameError = null,
                errorMessage = null,
            )
        }

        viewModelScope.launch {
            when (
                val result = authRepository.updateDisplayName(
                    normalizedName,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCompleted = true,
                        )
                    }
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

    private companion object {
        const val MIN_NAME_LENGTH = 2
        const val MAX_NAME_LENGTH = 120
        val WHITESPACE_REGEX = Regex("\\s+")
    }
}
