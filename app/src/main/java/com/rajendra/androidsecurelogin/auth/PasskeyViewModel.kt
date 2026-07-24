package com.rajendra.androidsecurelogin.auth

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasskeyViewModel(private val authenticator: PasskeyAuthenticator) : ViewModel() {

    private val _uiState = MutableStateFlow<PasskeyUiState>(PasskeyUiState.Idle)
    val uiState: StateFlow<PasskeyUiState> = _uiState.asStateFlow()

    fun register(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = PasskeyUiState.Loading
            val result = authenticator.registerPasskey(activity)
            result.onSuccess {
                _uiState.value = PasskeyUiState.Success(it)
            }.onFailure {
                _uiState.value = PasskeyUiState.Error(it.message ?: "Registration failed")
            }
        }
    }

    fun login(activity: FragmentActivity) {
        viewModelScope.launch {
            _uiState.value = PasskeyUiState.Loading
            val result = authenticator.loginWithPasskey(activity)
            result.onSuccess {
                _uiState.value = PasskeyUiState.Success(it)
            }.onFailure {
                _uiState.value = PasskeyUiState.Error(it.message ?: "Login failed")
            }
        }
    }

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

sealed class PasskeyUiState {
    object Idle : PasskeyUiState()
    object Loading : PasskeyUiState()
    data class Success(val message: String) : PasskeyUiState()
    data class Error(val message: String) : PasskeyUiState()
}
