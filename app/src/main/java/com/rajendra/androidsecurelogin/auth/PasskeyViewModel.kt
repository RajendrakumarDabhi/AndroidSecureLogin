package com.rajendra.androidsecurelogin.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for managing Passkey registration and login flows.
 *
 * This class coordinates between the UI and [PasskeyAuthenticator] to perform
 * passwordless authentication operations.
 */
class PasskeyViewModel(private val authenticator: PasskeyAuthenticator) : ViewModel() {

    private val _uiState = MutableStateFlow<PasskeyUiState>(PasskeyUiState.Idle)
    
    /**
     * Exposes the current Passkey operation state to the UI.
     */
    val uiState: StateFlow<PasskeyUiState> = _uiState.asStateFlow()

    /**
     * Triggers the Passkey registration flow.
     *
     * ## 🌐 Backend Communication (Registration Finish):
     * After [PasskeyAuthenticator.registerPasskey] succeeds, you must send the credential 
     * data to your server (e.g., `POST /auth/register/finish`):
     *
     * ```json
     * {
     *   "id": "base64-credential-id",
     *   "rawId": "base64-credential-id",
     *   "type": "public-key",
     *   "response": {
     *     "attestationObject": "base64-encoded-attestation",
     *     "clientDataJSON": "base64-encoded-client-data"
     *   },
     *   "authenticatorAttachment": "platform"
     * }
     * ```
     */
    fun register(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = PasskeyUiState.Loading
            val result = authenticator.registerPasskey(activity)
            result.onSuccess {
                // In production, send the credential bundle data to your API
                _uiState.value = PasskeyUiState.Success(it)
            }.onFailure {
                _uiState.value = PasskeyUiState.Error(it.message ?: "Registration failed")
            }
        }
    }

    /**
     * Triggers the Passkey login flow.
     *
     * ## 🌐 Backend Communication (Login Finish):
     * After [PasskeyAuthenticator.loginWithPasskey] succeeds, you must send the assertion 
     * to your server (e.g., `POST /auth/login/finish`):
     *
     * ```json
     * {
     *   "id": "base64-credential-id",
     *   "type": "public-key",
     *   "response": {
     *     "authenticatorData": "base64-encoded-auth-data",
     *     "clientDataJSON": "base64-encoded-client-data",
     *     "signature": "base64-encoded-signature",
     *     "userHandle": "base64-encoded-user-id"
     *   }
     * }
     * ```
     */
    fun login(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = PasskeyUiState.Loading
            val result = authenticator.loginWithPasskey(activity)
            result.onSuccess {
                // In production, send the assertion bundle data to your API
                _uiState.value = PasskeyUiState.Success(it)
            }.onFailure {
                _uiState.value = PasskeyUiState.Error(it.message ?: "Login failed")
            }
        }
    }

    /**
     * Resets the UI state to [PasskeyUiState.Idle].
     */
    fun resetState() {
        _uiState.value = PasskeyUiState.Idle
    }

    class Factory(private val authenticator: PasskeyAuthenticator) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PasskeyViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PasskeyViewModel(authenticator) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

/**
 * Represents the UI state for Passkey operations.
 */
sealed class PasskeyUiState {
    /** No operation in progress. */
    object Idle : PasskeyUiState()
    
    /** Registration or Login is currently being processed. */
    object Loading : PasskeyUiState()
    
    /** The operation completed successfully with a [message]. */
    data class Success(val message: String) : PasskeyUiState()
    
    /** An error occurred during the operation with an error [message]. */
    data class Error(val message: String) : PasskeyUiState()
}
