package com.konasl.fido2mobileapp.ui.screens

import android.R.attr.onClick
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.konasl.fido2mobileapp.MainActivity
import com.konasl.fido2mobileapp.network.ApiService
import com.konasl.fido2mobileapp.ui.components.AppTextField
import com.konasl.fido2mobileapp.ui.components.CircularProgressWithMessage
import com.konasl.fido2mobileapp.ui.components.ErrorBanner
import com.konasl.fido2mobileapp.ui.components.PrimaryButton
import com.konasl.fido2mobileapp.ui.theme.AccentPink
import com.konasl.fido2mobileapp.ui.theme.PrimaryPurple
import com.konasl.fido2mobileapp.utils.Constants
import java.net.URLEncoder

enum class AuthenticationState {
    INITIAL,
    FETCHING_CHALLENGE,
    AUTHENTICATING,
    VERIFYING,
    SUCCESS,
    ERROR
}

@Composable
fun LoginScreen(navController: NavController, showAuthDialog: (String, String) -> Unit) {
    val context = LocalContext.current
    val mainActivity = context as? MainActivity
    var username by remember { mutableStateOf("") }
    var currentUserName by remember { mutableStateOf("") }
    var authenticationState by remember { mutableStateOf(AuthenticationState.INITIAL) }
    
    // Observe authentication error state from MainActivity
    val authError by mainActivity?.authenticationError ?: remember { mutableStateOf<String?>(null) }
    
    // Effect to watch for errors and update state
    LaunchedEffect(authError) {
        if (authError != null) {
            authenticationState = AuthenticationState.ERROR
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Purple circle with login icon
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(PrimaryPurple),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Login,
                contentDescription = "Login",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
                    .clip(CircleShape)
                    .background(PrimaryPurple)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = "Sign in with your WebAuthn credentials",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Progress indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            val steps = 5
            for (i in 0 until steps) {
                val isActive = when (authenticationState) {
                    AuthenticationState.INITIAL -> i == 0
                    AuthenticationState.FETCHING_CHALLENGE -> i <= 1
                    AuthenticationState.AUTHENTICATING -> i <= 2
                    AuthenticationState.VERIFYING -> i <= 3
                    AuthenticationState.SUCCESS -> i <= 4
                    AuthenticationState.ERROR -> false
                }
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(12.dp)
                        .background(
                            color = if (isActive) PrimaryPurple else Color.LightGray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Show error banner if there's an error
        authError?.let { error ->
            ErrorBanner(
                message = error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        when (authenticationState) {
            AuthenticationState.INITIAL -> {
                // Username input field
                AppTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        mainActivity?.clearAuthenticationError()
                    },
                    labelText = "Username",
                    placeholderText = "your.email@example.com",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username",
                            tint = Color.Gray
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PrimaryButton(
                    onClick = {
                        if (username.isBlank()) {
                            mainActivity?.setAuthenticationError("Username is required")
                            return@PrimaryButton
                        }
                        
                        authenticationState = AuthenticationState.FETCHING_CHALLENGE
                        mainActivity?.clearAuthenticationError()
                        
                        val baseUrl = Constants.BASE_URL
                        val authenticatePath = Constants.FIDO2_AUTHENTICATION_PATH
                        val fullUrl = "$baseUrl$authenticatePath"
                        
                        val finalUsername = username.trim()
                        val encodedUsername = URLEncoder.encode(finalUsername, "UTF-8")
                        
                        val url = "$fullUrl?username=$encodedUsername"
                        
                        ApiService.get(url, emptyMap(), { success, response ->
                            try {
                                if (success && response != null) {
                                    authenticationState = AuthenticationState.AUTHENTICATING
                                    showAuthDialog(finalUsername, response)
                                } else {
                                    mainActivity?.setAuthenticationError("Failed to start authentication. Please try again.")
                                    Log.e("Authentication", "Failed to get authentication options: $response")
                                    authenticationState = AuthenticationState.ERROR
                                }
                            } catch (e: Exception) {
                                mainActivity?.setAuthenticationError("Authentication error: ${e.message}")
                                Log.e("Login", "User login failed: ${e.message}")
                                authenticationState = AuthenticationState.ERROR
                            }
                        })
                    },
                    text = "Sign In",
                    icon = Icons.Default.Login
                )
            }
            
            AuthenticationState.FETCHING_CHALLENGE -> {
                CircularProgressWithMessage(
                    message = "Preparing authentication...",
                    color = PrimaryPurple
                )
            }
            
            AuthenticationState.AUTHENTICATING -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Authentication",
                        tint = AccentPink,
                        modifier = Modifier.size(72.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Please authenticate using your registered method",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Use your fingerprint, face ID, or security key",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
            
            AuthenticationState.VERIFYING -> {
                CircularProgressWithMessage(
                    message = "Verifying your credentials...",
                    color = PrimaryPurple
                )
            }
            
            AuthenticationState.SUCCESS -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Login,
                        contentDescription = "Success",
                        tint = PrimaryPurple,
                        modifier = Modifier.size(72.dp)
                    )
                    
                    Text(
                        text = "Authentication Successful!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                    
                    Text(
                        text = "Redirecting to your profile...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
            
            AuthenticationState.ERROR -> {
                // Error state already handled by the error banner
                AppTextField(
                    value = username,
                    onValueChange = { 
                        username = it
                        mainActivity?.clearAuthenticationError()
                        authenticationState = AuthenticationState.INITIAL
                    },
                    labelText = "Username",
                    placeholderText = "your.email@example.com",
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Username",
                            tint = Color.Gray
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                PrimaryButton(
                    onClick = {
                        if (username.isBlank()) {
                            mainActivity?.setAuthenticationError("Username is required")
                            return@PrimaryButton
                        }
                        
                        authenticationState = AuthenticationState.FETCHING_CHALLENGE
                        mainActivity?.clearAuthenticationError()
                        
                        val baseUrl = Constants.BASE_URL
                        val authenticatePath = Constants.FIDO2_AUTHENTICATION_PATH
                        val fullUrl = "$baseUrl$authenticatePath"
                            mainActivity?.setAuthenticationError("Username is required")
                        val finalUsername = username.trim()
                        val encodedUsername = URLEncoder.encode(finalUsername, "UTF-8")
                        
                        val url = "$fullUrl?username=$encodedUsername"
                        
                        ApiService.get(url, emptyMap(), { success, response ->
                            try {
                                if (success && response != null) {
                                    authenticationState = AuthenticationState.AUTHENTICATING
                                    showAuthDialog(finalUsername, response)
                                } else {
                                    mainActivity?.setAuthenticationError("Failed to start authentication. Please try again.")
                                    authenticationState = AuthenticationState.ERROR
                                }
                            } catch (e: Exception) {
                                mainActivity?.setAuthenticationError("Authentication error: ${e.message}")
                                authenticationState = AuthenticationState.ERROR
                            }
                        })
                    },
                    text = "Try Again",
                    icon = Icons.Default.Login
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (authenticationState == AuthenticationState.INITIAL || authenticationState == AuthenticationState.ERROR) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                
                TextButton(onClick = { navController.navigate("register") }) {
                    Text(
                        text = "Register here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            TextButton(
                onClick = { navController.navigate("home") },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Back to Home")
            }
        }
    }
    
    // Update MainActivity's showAuthDialog function to update the authenticationState
    DisposableEffect(Unit) {
        val originalShowAuthDialog = showAuthDialog
        val wrappedShowAuthDialog: (String, String) -> Unit = { username, response ->
            authenticationState = AuthenticationState.AUTHENTICATING
            originalShowAuthDialog(username, response)
        }
        onDispose { }
    }
}
