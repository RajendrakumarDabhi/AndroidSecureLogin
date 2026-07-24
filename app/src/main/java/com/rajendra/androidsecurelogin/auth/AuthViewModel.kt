package com.rajendra.androidsecurelogin.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val authenticator: BiometricAuthenticator) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun authenticate(activity: FragmentActivity) {
        if (!authenticator.isBiometricAvailable()) {
            _authState.value = AuthState.Error("Biometric authentication is not supported or not set up")
            return
        }

        _authState.value = AuthState.Loading
        authenticator.promptBiometricAuth(
            activity = activity,
            title = "Biometric Login",
            subtitle = "Log in using your biometric credential",
            negativeButtonText = "Cancel",
            onSuccess = {
                _authState.value = AuthState.Success("Logged In Successfully (Mocked)")
            },
            onError = { errorCode, errString ->
                _authState.value = AuthState.Error("Authentication error: $errString ($errorCode)")
            },
            onFailed = {
                _authState.value = AuthState.Error("Authentication failed")
            }
        )
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    class Factory(private val authenticator: BiometricAuthenticator) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AuthViewModel(authenticator) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val message: String) : AuthState()
    data class Error(val message: String) : AuthState()
}
