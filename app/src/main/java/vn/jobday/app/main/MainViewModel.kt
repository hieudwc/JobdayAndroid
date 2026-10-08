package vn.jobday.app.main

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import vn.jobday.core.data.auth.AuthRepository
import vn.jobday.core.model.auth.AccountType
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authRepository: AuthRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val guestMode: StateFlow<Boolean> =
        savedStateHandle.getStateFlow(
            key = KEY_GUEST_MODE,
            initialValue = false,
        )

    private val session = authRepository.session
        .onEach { authenticatedSession ->
            if (authenticatedSession != null) {
                savedStateHandle[KEY_GUEST_MODE] = false
            }
        }

    val uiState: StateFlow<MainUiState> =
        combine(
            session,
            guestMode,
        ) { authenticatedSession, isGuest ->
            when {
                authenticatedSession != null &&
                    authenticatedSession.user.accountType ==
                    AccountType.CANDIDATE &&
                    authenticatedSession.user.displayName.isNullOrBlank() -> {
                    MainUiState.AuthenticatedNeedsOnboarding(
                        user = authenticatedSession.user,
                    )
                }

                authenticatedSession != null -> {
                    MainUiState.Authenticated(
                        user = authenticatedSession.user,
                    )
                }

                isGuest -> MainUiState.Guest

                else -> MainUiState.Unauthenticated
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = 5_000,
            ),
            initialValue = MainUiState.CheckingSession,
        )

    fun continueAsGuest() {
        savedStateHandle[KEY_GUEST_MODE] = true
    }

    private companion object {
        const val KEY_GUEST_MODE = "guest_mode"
    }
}
