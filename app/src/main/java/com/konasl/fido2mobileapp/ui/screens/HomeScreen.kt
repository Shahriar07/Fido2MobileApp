package com.konasl.fido2mobileapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController, onRegister: () -> Unit,
onLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to FIDO2 App")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onRegister() }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onLogin() }) {
            Text("Login")
        }
    }
}
