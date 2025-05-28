//package com.konasl.fido2mobileapp
//
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.browser.customtabs.CustomTabsIntent
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Surface
//import androidx.navigation.compose.rememberNavController
//import com.konasl.fido2mobileapp.ui.theme.Fido2MobileAppTheme
//import com.konasl.fido2mobileapp.ui.screens.NavigationGraph
//import net.openid.appauth.AuthorizationException
//import net.openid.appauth.AuthorizationRequest
//import net.openid.appauth.AuthorizationResponse
//import net.openid.appauth.AuthorizationService
//import net.openid.appauth.AuthorizationServiceConfiguration
//
//class MainActivity : ComponentActivity() {
//
//    private lateinit var authService: AuthorizationService
//    private lateinit var authRequest: AuthorizationRequest
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        authService = AuthorizationService(this)
//
//        setContent {
//            Fido2MobileAppTheme {
//                Surface(color = MaterialTheme.colorScheme.background) {
//                    val navController = rememberNavController()
//                    NavigationGraph(
//                        navController,
//                        onRegister =  { startAuthorizationFlow() },
//                        onLogin = { startAuthorizationFlow() }
//                    )
//                }
//            }
//        }
//
//        // Handle intent if activity is launched with deep link
//        intent?.let {
//            handleAuthorizationResponse(it)
//        }
//    }
//
//    private fun startAuthorizationFlow() {
//        val serviceConfig = AuthorizationServiceConfiguration(
//            Uri.parse("https://dev-12202026.okta.com/oauth2/v1/authorize"),
//            Uri.parse("https://dev-12202026.okta.com/oauth2/v1/token")
//        )
//
//        authRequest = AuthorizationRequest.Builder(
//            serviceConfig,
//            "0oaowg1kucupMxmmY5d7", // client_id
//            "code",
//            Uri.parse("com.konasl.fido2mobileapp:/callback")
//        )
//            .setScope("openid profile email")
//            .setState("abc124")
//            .setNonce("xyz789")
//            .setCodeVerifier("test_code_verifier_123") // Replace with secure value later
////            .setCodeChallenge("-KUirfHwlvcvFBRyu_YXRyIVHoKnNojxE6Ca-PV87ZQ")
////            .setCodeChallengeMethod("S256")
//            .build()
//
//        val authIntent = authService.getAuthorizationRequestIntent(
//            authRequest,
//            CustomTabsIntent.Builder().build()
//        )
//
//        startActivityForResult(authIntent, AUTH_REQUEST_CODE)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == AUTH_REQUEST_CODE) {
//            data?.let {
//                handleAuthorizationResponse(it)
//            }
//        }
//    }
//
//    override fun onNewIntent(intent: Intent?) {
//        super.onNewIntent(intent)
//        intent?.let {
//            handleAuthorizationResponse(it)
//        }
//    }
//
//    private fun handleAuthorizationResponse(intent: Intent) {
//        val response = AuthorizationResponse.fromIntent(intent)
//        val ex = AuthorizationException.fromIntent(intent)
//
//        if (response != null) {
//            val authCode = response.authorizationCode
//            val redirectUri = response.request.redirectUri
//
//            // TODO: Pass authCode to backend to complete WebAuthn registration
//            println("Received Authorization Code: $authCode")
//        } else {
//            ex?.let {
//                println("Authorization failed: ${it.error}")
//            }
//        }
//    }
//
//    companion object {
//        private const val AUTH_REQUEST_CODE = 1001
//    }
//}


package com.konasl.fido2mobileapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.konasl.fido2mobileapp.ui.theme.Fido2MobileAppTheme
import com.konasl.fido2mobileapp.ui.screens.HomeScreen
import com.konasl.fido2mobileapp.ui.screens.LoginScreen
import com.konasl.fido2mobileapp.ui.screens.RegisterScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

class MainActivity : ComponentActivity() {

    private lateinit var authIntent: Intent

    private val authLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            handleAuthorizationResponse(data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Fido2MobileAppTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController,  onRegister =  { startAuthorizationFlow() },
                        onLogin = { startAuthorizationFlow() })
                    }
                    composable("register") {
                        RegisterScreen(navController)
                    }
                    composable("login") {
                        LoginScreen(navController)
                    }
                }
            }
        }
    }

    private fun startAuthorizationFlow() {
        // Replace with your actual Auth URI setup logic
        val authUri = Uri.parse(
            "https://dev-12202026.okta.com/oauth2/v1/authorize?" +
                    "idp=0oaowiju4yrqv3ldC5d7&" +
                    "client_id=0oaowg1kucupMxmmY5d7&" +
                    "response_type=code&" +
                    "scope=openid&" +
                    "redirect_uri=com.konasl.fido2mobileapp:/callback&" +
                    "state=abc124&" +
                    "nonce=xyz789&" +
                    "code_challenge=-KUirfHwlvcvFBRyu_YXRyIVHoKnNojxE6Ca-PV87ZQ&" +
                    "code_challenge_method=S256"
        )

        val intent = Intent(Intent.ACTION_VIEW, authUri)
        authLauncher.launch(intent)
    }

    private fun handleAuthorizationResponse(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.toString().startsWith("com.konasl.fido2mobileapp:/callback")) {
            val authCode = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")
            println("Authorization Code: $authCode")
            println("State: $state")
            // TODO: Exchange code for tokens or initiate WebAuthn registration
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleAuthorizationResponse(intent)
    }
}
