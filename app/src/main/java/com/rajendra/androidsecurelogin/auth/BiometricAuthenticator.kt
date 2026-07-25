package com.rajendra.androidsecurelogin.auth

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Handles Biometric authentication logic.
 *
 * ## Backend Communication Flow:
 * To securely verify biometric success on a backend, follow these steps:
 * 1. **Challenge-Response**: Request a unique "challenge" (nonce) from your backend.
 * 2. **Cryptographic Signing**: Use a [BiometricPrompt.CryptoObject] initialized with a Signature 
 *    or Cipher from the Android Keystore. This key should be configured to require user 
 *    authentication (`setUserAuthenticationRequired(true)`).
 * 3. **Success**: In [onAuthenticationSucceeded], use the unlocked [BiometricPrompt.AuthenticationResult.cryptoObject] 
 *    to sign the challenge.
 * 4. **Verification**: Send the signature and the challenge back to the backend. The backend 
 *    verifies the signature using the public key previously registered for this user.
 *
 * *Note: If no CryptoObject is used, the authentication is only local to the device and 
 * cannot be verified securely by a remote server.*
 */
class BiometricAuthenticator(private val context: Context) {

    /**
     * Checks if the device supports biometric authentication and if any are enrolled.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Triggers the biometric prompt.
     *
     * @param activity The [FragmentActivity] used to host the prompt.
     * @param title Title for the biometric dialog.
     * @param subtitle Subtitle for the biometric dialog.
     * @param negativeButtonText Text for the cancel button.
     * @param onSuccess Callback triggered when user is successfully authenticated. 
     *                  Receive [BiometricPrompt.AuthenticationResult] to access CryptoObject for backend verification.
     * @param onError Callback for unrecoverable errors (e.g., no biometrics enrolled).
     * @param onFailed Callback for recoverable failures (e.g., fingerprint not recognized).
     */
    fun promptBiometricAuth(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (Int, CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
