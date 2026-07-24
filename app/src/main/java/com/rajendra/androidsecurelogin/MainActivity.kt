package com.rajendra.androidsecurelogin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajendra.androidsecurelogin.auth.AuthViewModel
import com.rajendra.androidsecurelogin.auth.BiometricAuthenticator
import com.rajendra.androidsecurelogin.auth.PasskeyAuthenticator
import com.rajendra.androidsecurelogin.auth.PasskeyViewModel
import com.rajendra.androidsecurelogin.ui.LoginScreen
import com.rajendra.androidsecurelogin.ui.PasskeyLoginScreen
import com.rajendra.androidsecurelogin.ui.theme.AndroidSecureLoginTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSecureLoginTheme {
                val context = LocalContext.current
                
                // Biometric Setup
                val biometricAuthenticator = remember { BiometricAuthenticator(context.applicationContext) }
                val biometricViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(biometricAuthenticator)
                )

                // Passkey Setup
                val passkeyAuthenticator = remember { PasskeyAuthenticator(context.applicationContext) }
                val passkeyViewModel: PasskeyViewModel = viewModel(
                    factory = PasskeyViewModel.Factory(passkeyAuthenticator)
                )

                var currentScreen by remember { mutableStateOf<Screen>(Screen.Main) }

                when (currentScreen) {
                    Screen.Main -> MainScreen(
                        onNavigateToBiometric = { currentScreen = Screen.Biometric },
                        onNavigateToPasskey = { currentScreen = Screen.Passkey }
                    )
                    Screen.Biometric -> LoginScreen(
                        viewModel = biometricViewModel,
                        onBack = { 
                            biometricViewModel.resetState()
                            currentScreen = Screen.Main 
                        }
                    )
                    Screen.Passkey -> PasskeyLoginScreen(
                        viewModel = passkeyViewModel,
                        onBack = {
                            passkeyViewModel.resetState()
                            currentScreen = Screen.Main
                        }
                    )
                }
            }
        }
    }
}

sealed class Screen {
    data object Main : Screen()
    data object Biometric : Screen()
    data object Passkey : Screen()
}

@Composable
fun MainScreen(
    onNavigateToBiometric: () -> Unit,
    onNavigateToPasskey: () -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Secure Login Demo", style = MaterialTheme.typography.headlineMedium)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToBiometric,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Biometric Authentication")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onNavigateToPasskey,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("Passkey Authentication")
            }
        }
    }
}
