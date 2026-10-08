package vn.jobday.app.main

import vn.jobday.core.model.auth.User

sealed interface MainUiState {

    data object CheckingSession : MainUiState

    data object Unauthenticated : MainUiState

    data object Guest : MainUiState

    data class AuthenticatedNeedsOnboarding(
        val user: User,
    ) : MainUiState

    data class Authenticated(
        val user: User,
    ) : MainUiState
}
