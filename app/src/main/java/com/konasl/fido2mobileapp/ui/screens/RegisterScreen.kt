package com.konasl.fido2mobileapp.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.konasl.fido2mobileapp.network.ApiService
import com.konasl.fido2mobileapp.ui.components.AppTextField
import com.konasl.fido2mobileapp.ui.components.AppTopBar
import com.konasl.fido2mobileapp.ui.components.CardContainer
import com.konasl.fido2mobileapp.ui.components.ErrorBanner
import com.konasl.fido2mobileapp.ui.components.IconCircle
import com.konasl.fido2mobileapp.ui.components.PrimaryButton
import com.konasl.fido2mobileapp.ui.components.SecureFooter
import com.konasl.fido2mobileapp.ui.theme.AccentPink
import com.konasl.fido2mobileapp.ui.theme.BackgroundLight
import com.konasl.fido2mobileapp.ui.theme.PrimaryPurple
import com.konasl.fido2mobileapp.ui.theme.TextPrimary
import com.konasl.fido2mobileapp.ui.theme.TextSecondary
import com.konasl.fido2mobileapp.utils.Constants
import java.net.URLEncoder

@Composable
fun RegisterScreen(navController: NavController, showFidoDialog: (String, String) -> Unit) {
    val (username, setUserName) = remember { mutableStateOf("") }
    val (display, setDisplay) = remember { mutableStateOf("") }

    // Error state
    val (errorMessage, setErrorMessage) = remember { mutableStateOf<String?>(null) }

    // Registration state
    val (registrationState, setRegistrationState) = remember {
        mutableStateOf(RegistrationState.USER_INFO)
    }

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
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTopBar()

            Spacer(modifier = Modifier.height(24.dp))

            CardContainer {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Pink circle with person icon
                    IconCircle(
                        icon = Icons.Default.PersonAdd,
                        color = AccentPink,
                        contentDescription = "Create Account"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Set up your secure WebAuthn account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress indicators for registration steps
                    StepProgressBar(
                        currentStep = registrationState.ordinal + 1,
                        totalSteps = 5,
                        stepLabels = listOf(
                            "User Info",
                            "Processing...",
                            "Authenticating",
                            "Verifying",
                            "Complete"
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Show error message if present
                    errorMessage?.let {
                        ErrorBanner(
                            message = it,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // User info form
                    AppTextField(
                        value = username,
                        onValueChange = {
                            setUserName(it)
                            setErrorMessage(null) // Clear error when user types
                        },
                        labelText = "Username",
                        placeholderText = "your.email@example.com",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Username Icon",
                                tint = TextSecondary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AppTextField(
                        value = display,
                        onValueChange = {
                            setDisplay(it)
                            setErrorMessage(null) // Clear error when user types
                        },
                        labelText = "Display Name",
                        placeholderText = "Your full name",
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Display Name Icon",
                                tint = TextSecondary
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryButton(
                        onClick = {
                            // Validate input
                            when {
                                username.isBlank() -> {
                                    setErrorMessage("Username is required")
                                }

                                display.isBlank() -> {
                                    setErrorMessage("Display name is required")
                                }

                                else -> {
                                    setErrorMessage(null)
                                    setRegistrationState(RegistrationState.PROCESSING)

                                    // Start registration process
                                    val baseUrl = Constants.BASE_URL
                                    val registerPath = Constants.FIDO2_REGISTER_PATH
                                    val fullUrl = "$baseUrl$registerPath"

                                    val finalUsername = username.trim()
                                    val finalDisplay = display.trim()

                                    val encodedUsername = URLEncoder.encode(finalUsername, "UTF-8")
                                    val encodedDisplay = URLEncoder.encode(finalDisplay, "UTF-8")

                                    val url =
                                        "$fullUrl?username=$encodedUsername&displayName=$encodedDisplay"

                                    ApiService.get(url, emptyMap(), { success, response ->
                                        try {
                                            if (success && response != null) {
                                                setRegistrationState(RegistrationState.AUTHENTICATOR)
                                                showFidoDialog(finalUsername, response)
                                            } else {
                                                setRegistrationState(RegistrationState.USER_INFO)
                                                setErrorMessage("Registration failed. Please try again.")
                                                Log.e(
                                                    "TokenExchange",
                                                    "Failed to exchange token $response"
                                                )
                                            }
                                        } catch (e: Exception) {
                                            setRegistrationState(RegistrationState.USER_INFO)
                                            setErrorMessage("Registration error: ${e.message}")
                                            Log.e(
                                                "Register",
                                                "Register device failed : ${e.message}"
                                            )
                                        }
                                    })
                                }
                            }
                        },
                        text = "Create Account",
                        icon = Icons.Default.PersonAdd
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Already have an account? ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        TextButton(onClick = { navController.navigate("login") }) {
                            Text(
                                "Login here",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            SecureFooter()
        }
    }
}

// Create a composable for the step progress bar
@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    stepLabels: List<String>
) {
    Column {
        // Step circles
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            for (i in 1..totalSteps) {
                val isActive = i <= currentStep
                val color = if (isActive) AccentPink else Color.LightGray.copy(alpha = 0.5f)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(color = color, shape = MaterialTheme.shapes.small)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Step icon or number could go here
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stepLabels[i - 1],
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) TextPrimary else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

enum class RegistrationState {
    USER_INFO,
    PROCESSING,
    AUTHENTICATOR,
    VERIFYING,
    COMPLETE
}
