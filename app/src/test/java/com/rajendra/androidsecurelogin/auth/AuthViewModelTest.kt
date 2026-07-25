package com.rajendra.androidsecurelogin.auth

import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authenticator: BiometricAuthenticator
    private lateinit var viewModel: AuthViewModel
    private val activity: FragmentActivity = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authenticator = mockk()
        viewModel = AuthViewModel(authenticator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when biometric is not available, state should be error`() = runTest {
        every { authenticator.isBiometricAvailable() } returns false

        viewModel.authState.test {
            assertEquals(AuthState.Idle, awaitItem())
            viewModel.authenticate(activity)
            val errorState = awaitItem() as AuthState.Error
            assertEquals("Biometric authentication is not supported or not set up", errorState.message)
        }
    }

    @Test
    fun `when authentication starts, state should be loading`() = runTest {
        every { authenticator.isBiometricAvailable() } returns true
        every {
            authenticator.promptBiometricAuth(any(), any(), any(), any(), any(), any(), any())
        } returns Unit

        viewModel.authState.test {
            assertEquals(AuthState.Idle, awaitItem())
            viewModel.authenticate(activity)
            assertEquals(AuthState.Loading, awaitItem())
        }
    }

    @Test
    fun `when authentication succeeds, state should be success`() = runTest {
        val successSlot = slot<(BiometricPrompt.AuthenticationResult) -> Unit>()
        every { authenticator.isBiometricAvailable() } returns true
        every {
            authenticator.promptBiometricAuth(any(), any(), any(), any(), capture(successSlot), any(), any())
        } answers {
            successSlot.captured(mockk())
        }

        viewModel.authState.test {
            assertEquals(AuthState.Idle, awaitItem())
            viewModel.authenticate(activity)
            assertEquals(AuthState.Loading, awaitItem())
            val successState = awaitItem() as AuthState.Success
            assertEquals("Logged In Successfully (Mocked)", successState.message)
        }
    }

    @Test
    fun `when authentication errors, state should be error`() = runTest {
        val errorSlot = slot<(Int, CharSequence) -> Unit>()
        val errorMsg = "Hardware not present"
        every { authenticator.isBiometricAvailable() } returns true
        every {
            authenticator.promptBiometricAuth(any(), any(), any(), any(), any(), capture(errorSlot), any())
        } answers {
            errorSlot.captured(1, errorMsg)
        }

        viewModel.authState.test {
            assertEquals(AuthState.Idle, awaitItem())
            viewModel.authenticate(activity)
            assertEquals(AuthState.Loading, awaitItem())
            val errorState = awaitItem() as AuthState.Error
            assertEquals("Authentication error: $errorMsg (1)", errorState.message)
        }
    }

    @Test
    fun `when authentication fails, state should be error`() = runTest {
        val failedSlot = slot<() -> Unit>()
        every { authenticator.isBiometricAvailable() } returns true
        every {
            authenticator.promptBiometricAuth(any(), any(), any(), any(), any(), any(), capture(failedSlot))
        } answers {
            failedSlot.captured()
        }

        viewModel.authState.test {
            assertEquals(AuthState.Idle, awaitItem())
            viewModel.authenticate(activity)
            assertEquals(AuthState.Loading, awaitItem())
            val errorState = awaitItem() as AuthState.Error
            assertEquals("Authentication failed", errorState.message)
        }
    }

    @Test
    fun `when resetState is called, state should return to idle`() = runTest {
        every { authenticator.isBiometricAvailable() } returns false
        viewModel.authenticate(activity)

        viewModel.authState.test {
            assert(awaitItem() is AuthState.Error)
            viewModel.resetState()
            assertEquals(AuthState.Idle, awaitItem())
        }
    }
}
