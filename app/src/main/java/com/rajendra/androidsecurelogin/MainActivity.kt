package com.rajendra.androidsecurelogin

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigateToBiometric: () -> Unit,
    onNavigateToPasskey: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Login Demo") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Choose your preferred secure login method",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            LoginMethodCard(
                title = "Biometric Login",
                description = "Quick and secure access using your fingerprint or face scan.",
                icon = Icons.Default.Fingerprint,
                onClick = onNavigateToBiometric
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LoginMethodCard(
                title = "Passkey Login",
                description = "Modern, passwordless security synced across your devices.",
                icon = Icons.Default.Key,
                onClick = onNavigateToPasskey
            )
        }
    }
}

@Composable
fun LoginMethodCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
