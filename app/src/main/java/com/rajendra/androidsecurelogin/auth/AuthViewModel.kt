package com.rajendra.androidsecurelogin.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for handling Biometric Authentication states and logic.
 *
 * This class orchestrates the biometric flow between the UI and the [BiometricAuthenticator].
 * In a real-world scenario, the successful authentication result should be verified 
 * by a backend server before considering the login complete.
 */
class AuthViewModel(private val authenticator: BiometricAuthenticator) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    
    /**
     * Exposes the current authentication state to the UI.
     */
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    /**
     * Starts the biometric authentication process.
     *
     * ## Real-world Implementation Note:
     * To verify authentication on the backend:
     * 1. **Loading State**: Show a progress indicator while waiting for the user.
     * 2. **Success Case**: Instead of just setting `AuthState.Success`, you would:
     *    - Extract the signature/assertion from `AuthenticationResult`.
     *    - Call a repository/API to send this signature to your backend.
     *    - The backend verifies the signature against the stored public key.
     *    - Upon backend success, transition to the authenticated part of the app.
     */
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
            onSuccess = { result ->
                // MOCKED SUCCESS: In production, send 'result.cryptoObject' data to backend
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

    /**
     * Resets the authentication state to [AuthState.Idle].
     */
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

/**
 * Represents the various states of the Biometric authentication flow.
 */
sealed class AuthState {
    /** The initial state, or state after a reset. */
    object Idle : AuthState()
    
    /** Indicates the authentication prompt is active or backend verification is in progress. */
    object Loading : AuthState()
    
    /** Indicates authentication was successful. Contains a success [message]. */
    data class Success(val message: String) : AuthState()
    
    /** Indicates an error occurred. Contains an error [message]. */
    data class Error(val message: String) : AuthState()
}
