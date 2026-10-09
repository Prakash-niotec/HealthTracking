package com.healthtrack.app.ui.model


sealed class AuthState {
    data object Unauthenticated : AuthState()

    data object Loading : AuthState()

    data class Authenticated(val userProfile: UserProfile) : AuthState()

    data class Error(val message: String) : AuthState()
}
