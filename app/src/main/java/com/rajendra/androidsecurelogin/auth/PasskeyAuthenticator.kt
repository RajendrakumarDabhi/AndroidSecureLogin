package com.rajendra.androidsecurelogin.auth

import android.content.Context
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity

/**
 * Handles Passkey (WebAuthn/FIDO2) authentication logic using Android Credential Manager.
 *
 * ## Backend Communication Flow:
 *
 * ### 1. Passkey Registration (Create Credential)
 * 1. **Challenge Request**: The app requests a registration challenge from the backend.
 * 2. **Backend Options**: The backend returns a JSON (like [registerRequestJson]).
 * 3. **Client Execution**: The app calls [registerPasskey].
 * 4. **Backend Verification**: Send the resulting credential data to your `/register/finish` endpoint.
 *
 * #### Typical Registration Finish JSON:
 * ```json
 * {
 *   "id": "base64-credential-id",
 *   "rawId": "base64-credential-id",
 *   "type": "public-key",
 *   "response": {
 *     "attestationObject": "base64-attestation-data",
 *     "clientDataJSON": "base64-client-data-json"
 *   },
 *   "authenticatorAttachment": "platform"
 * }
 * ```
 *
 * ### 2. Passkey Login (Get Credential)
 * 1. **Challenge Request**: The app requests a login challenge from the backend.
 * 2. **Backend Options**: The backend returns a JSON (like [loginRequestJson]).
 * 3. **Client Execution**: The app calls [loginWithPasskey].
 * 4. **Backend Verification**: Send the assertion to your `/login/finish` endpoint.
 *
 * #### Typical Login Finish JSON (Assertion):
 * ```json
 * {
 *   "id": "base64-credential-id",
 *   "rawId": "base64-credential-id",
 *   "type": "public-key",
 *   "response": {
 *     "authenticatorData": "base64-auth-data",
 *     "clientDataJSON": "base64-client-data-json",
 *     "signature": "base64-signature",
 *     "userHandle": "base64-user-id"
 *   }
 * }
 * ```
 */
class PasskeyAuthenticator(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    /**
     * Mocked registration request JSON. 
     * In a real app, this should be fetched from your backend's registration-start endpoint.
     */
    private val registerRequestJson = """
        {
            "challenge": "bm9uY2U",
            "rp": {
                "name": "Secure Login App",
                "id": "secure-login-app.example.com"
            },
            "user": {
                "id": "dXNlcklk",
                "name": "user@example.com",
                "displayName": "Test User"
            },
            "pubKeyCredParams": [
                {
                    "type": "public-key",
                    "alg": -7
                }
            ],
            "timeout": 60000,
            "attestation": "none",
            "excludeCredentials": [],
            "authenticatorSelection": {
                "authenticatorAttachment": "platform",
                "requireResidentKey": true,
                "userVerification": "required"
            }
        }
    """.trimIndent()

    /**
     * Mocked login request JSON.
     * In a real app, this should be fetched from your backend's login-start endpoint.
     */
    private val loginRequestJson = """
        {
            "challenge": "bm9uY2U",
            "timeout": 60000,
            "rpId": "secure-login-app.example.com",
            "userVerification": "required"
        }
    """.trimIndent()

    /**
     * Executes the passkey registration flow.
     * 
     * @return A [Result] containing a success message or the exception.
     * In a real implementation, the success result would be the JSON response from 
     * [CredentialManager.createCredential] to be sent to the backend.
     */
    suspend fun registerPasskey(activity: FragmentActivity): Result<String> {
        return try {
            val request = CreatePublicKeyCredentialRequest(registerRequestJson)
            val result = credentialManager.createCredential(activity, request)
            // result.data contains the bundle that should be converted to JSON and sent to the backend.
            Result.success("Passkey registered: ${result.type}")
        } catch (e: CreateCredentialException) {
            Result.failure(e)
        }
    }

    /**
     * Executes the passkey login flow.
     * 
     * @return A [Result] containing a success message or the exception.
     * In a real implementation, the success result would be the [androidx.credentials.GetCredentialResponse]
     * to be sent to the backend for verification.
     */
    suspend fun loginWithPasskey(activity: FragmentActivity): Result<String> {
        return try {
            val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(loginRequestJson)
            val getCredentialRequest = GetCredentialRequest(
                listOf(getPublicKeyCredentialOption)
            )
            val result = credentialManager.getCredential(activity, getCredentialRequest)
            // result.credential.data contains the assertion to be converted to JSON and sent to the backend.
            Result.success("Logged in with passkey: ${result.credential.type}")
        } catch (e: GetCredentialException) {
            Result.failure(e)
        }
    }
}
