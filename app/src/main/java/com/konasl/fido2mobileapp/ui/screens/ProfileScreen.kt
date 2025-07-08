package com.konasl.fido2mobileapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun ProfileScreen(
    navController: NavController,
    userName: String,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, $userName")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            Log.i("Logout", "Logout")


//            val baseUrl = Constants.BASE_URL
//            val registerPath = Constants.FIDO2_REGISTER_PATH
//            val fullUrl = "$baseUrl$registerPath"
//
//            val finalUsername = username.trim()
//            val finalDisplay = display.trim()
//
//            val encodedUsername = URLEncoder.encode(finalUsername, "UTF-8")
//            val encodedDisplay = URLEncoder.encode(finalDisplay, "UTF-8")

//            val url = "$fullUrl?username=$encodedUsername&displayName=$encodedDisplay"
//            ApiService.get(url, emptyMap(), { success, response ->
//                try {
//                    if (success && response != null) {
//                        showFidoDialog(finalUsername, response)
//                    } else {
//                        Log.e("TokenExchange", "Failed to exchange token $response")
//                    }
//                } catch (e: Exception) {
//                    // Handle the exception
//                    Log.e("Register", "Register device failed : ${e.message}")
//                }

        }) {
            Text("Logout")
        }
    }
}

