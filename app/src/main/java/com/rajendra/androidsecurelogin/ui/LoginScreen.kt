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
import com.rajendra.androidsecurelogin.auth.AuthState
import com.rajendra.androidsecurelogin.auth.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
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
            Text(text = "Biometric Authentication", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(32.dp))

            when (val state = authState) {
                is AuthState.Idle -> {
                    Text(text = "Please authenticate to continue")
                }
                is AuthState.Loading -> {
                    CircularProgressIndicator()
                }
                is AuthState.Success -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.primary)
                }
                is AuthState.Error -> {
                    Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    LaunchedEffect(state) {
                        Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.authenticate(activity) },
                enabled = authState !is AuthState.Loading
            ) {
                Text("Authenticate Now")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = onBack) {
                Text("Back to Main")
            }
        }
    }
}
