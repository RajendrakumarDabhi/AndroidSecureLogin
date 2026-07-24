package com.rajendra.androidsecurelogin.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.rajendra.androidsecurelogin.auth.PasskeyUiState
import com.rajendra.androidsecurelogin.auth.PasskeyViewModel

@Composable
fun PasskeyLoginScreen(
    viewModel: PasskeyViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as FragmentActivity

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Passkey Authentication", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(32.dp))

            when (val state = uiState) {
                is PasskeyUiState.Idle -> {
                    Text(text = "Register or Login with Passkey")
                }
                is PasskeyUiState.Loading -> {
                    CircularProgressIndicator()
                }
                is PasskeyUiState.Success -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.primary)
                }
                is PasskeyUiState.Error -> {
                    Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    LaunchedEffect(state) {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.register(activity) },
                enabled = uiState !is PasskeyUiState.Loading,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text("Register Passkey")
            }

            Button(
                onClick = { viewModel.login(activity) },
                enabled = uiState !is PasskeyUiState.Loading
            ) {
                Text("Login with Passkey")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text("Back to Main")
            }
        }
    }
}
