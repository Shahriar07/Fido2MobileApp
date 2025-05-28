package com.konasl.fido2mobileapp.ui.screens

import androidx.navigation.compose.NavHost
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable

@Composable
fun NavigationGraph(
    navController: NavHostController, onRegister: () -> Unit,
    onLogin: () -> Unit
) {


    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                navController, onRegister = onRegister,
                onLogin = onLogin
            )
        }
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
    }
}
