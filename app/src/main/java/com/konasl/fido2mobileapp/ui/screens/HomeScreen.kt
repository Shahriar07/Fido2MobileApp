package com.konasl.fido2mobileapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.konasl.fido2mobileapp.ui.components.*
import com.konasl.fido2mobileapp.ui.theme.*

@Composable
fun HomeScreen(navController: NavController) {
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
            
            Spacer(modifier = Modifier.height(48.dp))
            
            IconCircle(
                icon = Icons.Default.Fingerprint,
                color = PrimaryPurple,
                modifier = Modifier.size(72.dp),
                contentDescription = "Fingerprint Icon"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "KONA Identity Provider",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryPurple
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Experience the future of authentication with biometric security, hardware keys, and passwordless login technology.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecondaryButton(
                    onClick = { navController.navigate("register") },
                    text = "Register",
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                
                PrimaryButton(
                    onClick = { navController.navigate("login") },
                    text = "Login",
                    icon = Icons.Default.Login,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            SecureFooter()
        }
    }
}
