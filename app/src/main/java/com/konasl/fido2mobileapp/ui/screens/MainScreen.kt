package com.konasl.fido2mobileapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MainScreen() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to FIDO2 Mobile App")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { /* TODO: Trigger registration */ }) {
            Text("Register with NFC")
        }
    }
}
