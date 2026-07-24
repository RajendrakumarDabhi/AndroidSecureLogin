package com.rajendra.androidsecurelogin.auth

import android.content.Context
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.fragment.app.FragmentActivity

class PasskeyAuthenticator(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)

    // Mocked registration request JSON (FIDO2)
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

    // Mocked login request JSON
    private val loginRequestJson = """
        {
            "challenge": "bm9uY2U",
            "timeout": 60000,
            "rpId": "secure-login-app.example.com",
            "userVerification": "required"
        }
    """.trimIndent()

    suspend fun registerPasskey(activity: FragmentActivity): Result<String> {
        return try {
            val request = CreatePublicKeyCredentialRequest(registerRequestJson)
            val result = credentialManager.createCredential(activity, request)
            Result.success("Passkey registered: ${result.type}")
        } catch (e: CreateCredentialException) {
            Result.failure(e)
        }
    }

    suspend fun loginWithPasskey(activity: FragmentActivity): Result<String> {
        return try {
            val getPublicKeyCredentialOption = GetPublicKeyCredentialOption(loginRequestJson)
            val getCredentialRequest = GetCredentialRequest(
                listOf(getPublicKeyCredentialOption)
            )
            val result = credentialManager.getCredential(activity, getCredentialRequest)
            Result.success("Logged in with passkey: ${result.credential.type}")
        } catch (e: GetCredentialException) {
            Result.failure(e)
        }
    }
}
