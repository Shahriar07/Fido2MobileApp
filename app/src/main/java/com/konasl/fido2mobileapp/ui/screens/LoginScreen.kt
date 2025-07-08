package com.konasl.fido2mobileapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.konasl.fido2mobileapp.network.ApiService
import com.konasl.fido2mobileapp.ui.components.*
import com.konasl.fido2mobileapp.ui.theme.*
import com.konasl.fido2mobileapp.utils.Constants
import java.net.URLEncoder

@Composable
fun LoginScreen(navController: NavController, showAuthDialog: (String, String) -> Unit) {
    val (username, setUserName) = remember { mutableStateOf("") }
    
    // Create a gradient background that matches the web design
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            BackgroundLight,
            BackgroundLight.copy(alpha = 0.8f)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTopBar()
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CardContainer {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Purple circle with login icon
                    IconCircle(
                        icon = Icons.Default.Login,
                        color = PrimaryPurple,
                        contentDescription = "Login"
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Sign in with your WebAuthn credentials",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Progress indicator
                    LinearProgressIndicator(
                        progress = { 0.2f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        color = PrimaryPurple
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        repeat(5) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (index == 0) PrimaryPurple else TextHint.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Username field
                    AppTextField(
                        value = username,
                        onValueChange = { setUserName(it) },
                        labelText = "Username",
                        placeholderText = "john@example.com",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User Icon",
                                tint = TextSecondary
                            )
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Login button
                    PrimaryButton(
                        onClick = {
                            val baseUrl = Constants.BASE_URL
                            val authenticatePath = Constants.FIDO2_AUTHENTICATION_PATH
                            val fullUrl = "$baseUrl$authenticatePath"

                            val finalUsername = username.trim()
                            val encodedUsername = URLEncoder.encode(finalUsername, "UTF-8")

                            val url = "$fullUrl?username=$encodedUsername"
                            ApiService.get(url, emptyMap(), { success, response ->
                                try {
                                    if (success && response != null) {
                                        showAuthDialog(finalUsername, response)
                                    } else {
                                        Log.e("TokenExchange", "Failed to exchange token $response")
                                    }
                                } catch (e: Exception) {
                                    Log.e("Login", "user login failed : ${e.message}")
                                }
                            })
                        },
                        text = "Sign In",
                        icon = Icons.Default.Login
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Don't have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TextButton(onClick = { navController.navigate("register") }) {
                            Text(
                                "Register now",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SecureFooter()
                }
            }
        }
    }
}
