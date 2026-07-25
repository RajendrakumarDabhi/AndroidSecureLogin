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
     * ## 🌐 Backend Communication (Login Verification):
     * To securely verify a biometric login, you should send the following payload to your API 
     * (e.g., `POST /api/auth/biometric-verify`):
     *
     * ```json
     * {
     *   "challenge": "the-nonce-from-server",
     *   "signature": "base64-encoded-signature",
     *   "userId": "user_id_123"
     * }
     * ```
     *
     * ### Implementation Steps:
     * 1. **Request Challenge**: Fetch a fresh challenge from your server.
     * 2. **Local Auth**: Pass a [androidx.biometric.BiometricPrompt.CryptoObject] to [BiometricAuthenticator.promptBiometricAuth].
     * 3. **Sign**: In the `onSuccess` callback, use the unlocked [androidx.biometric.BiometricPrompt.AuthenticationResult.cryptoObject] 
     *    to sign the challenge.
     * 4. **Verify**: Send the resulting signature to your server.
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
                // MOCKED SUCCESS: In production, sign your server's challenge using result.cryptoObject
                // and send the signature via your API.
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
