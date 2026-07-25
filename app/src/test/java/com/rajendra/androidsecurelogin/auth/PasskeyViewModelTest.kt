package com.rajendra.androidsecurelogin.auth

import androidx.fragment.app.FragmentActivity
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.mockk
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
class PasskeyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authenticator: PasskeyAuthenticator
    private lateinit var viewModel: PasskeyViewModel
    private val activity: FragmentActivity = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authenticator = mockk()
        viewModel = PasskeyViewModel(authenticator)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `when registration succeeds, state should be success`() = runTest {
        val successMessage = "Passkey registered"
        coEvery { authenticator.registerPasskey(activity) } returns Result.success(successMessage)

        viewModel.uiState.test {
            assertEquals(PasskeyUiState.Idle, awaitItem())
            viewModel.register(activity)
            assertEquals(PasskeyUiState.Loading, awaitItem())
            val successState = awaitItem() as PasskeyUiState.Success
            assertEquals(successMessage, successState.message)
        }
    }

    @Test
    fun `when registration fails, state should be error`() = runTest {
        val errorMessage = "Registration failed"
        coEvery { authenticator.registerPasskey(activity) } returns Result.failure(Exception(errorMessage))

        viewModel.uiState.test {
            assertEquals(PasskeyUiState.Idle, awaitItem())
            viewModel.register(activity)
            assertEquals(PasskeyUiState.Loading, awaitItem())
            val errorState = awaitItem() as PasskeyUiState.Error
            assertEquals(errorMessage, errorState.message)
        }
    }

    @Test
    fun `when login succeeds, state should be success`() = runTest {
        val successMessage = "Logged in"
        coEvery { authenticator.loginWithPasskey(activity) } returns Result.success(successMessage)

        viewModel.uiState.test {
            assertEquals(PasskeyUiState.Idle, awaitItem())
            viewModel.login(activity)
            assertEquals(PasskeyUiState.Loading, awaitItem())
            val successState = awaitItem() as PasskeyUiState.Success
            assertEquals(successMessage, successState.message)
        }
    }

    @Test
    fun `when login fails, state should be error`() = runTest {
        val errorMessage = "Login failed"
        coEvery { authenticator.loginWithPasskey(activity) } returns Result.failure(Exception(errorMessage))

        viewModel.uiState.test {
            assertEquals(PasskeyUiState.Idle, awaitItem())
            viewModel.login(activity)
            assertEquals(PasskeyUiState.Loading, awaitItem())
            val errorState = awaitItem() as PasskeyUiState.Error
            assertEquals(errorMessage, errorState.message)
        }
    }

}
