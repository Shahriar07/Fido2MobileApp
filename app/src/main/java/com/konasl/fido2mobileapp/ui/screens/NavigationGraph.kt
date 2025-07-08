package com.konasl.fido2mobileapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navController: NavHostController,
    showDialog: (String, String) -> Unit,
    userName: String = "",
    onLogout: () -> Unit = {}
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("register") { RegisterScreen(navController, showDialog) }
        composable("login") { LoginScreen(navController, showDialog) }
        composable("profile") {
            ProfileScreen(
                navController = navController,
                userName = userName,
                onLogout = onLogout
            )
        }
    }
}
