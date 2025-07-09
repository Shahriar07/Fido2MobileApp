package com.konasl.fido2mobileapp

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.AuthenticatorAssertionResponse
import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredential
import com.konasl.fido2mobileapp.fido.Fido2OptionDataBuilder
import com.konasl.fido2mobileapp.network.ApiService
import com.konasl.fido2mobileapp.ui.screens.HomeScreen
import com.konasl.fido2mobileapp.ui.screens.LoginScreen
import com.konasl.fido2mobileapp.ui.screens.ProfileScreen
import com.konasl.fido2mobileapp.ui.screens.RegisterScreen
import com.konasl.fido2mobileapp.ui.theme.Fido2MobileAppTheme
import com.konasl.fido2mobileapp.utils.Constants
import com.konasl.fido2mobileapp.utils.Fido2Utils
import com.konasl.fido2mobileapp.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    // Define the constant here
    private companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
        private const val LOG_TAG = "Fido2Demo"
        private const val KEY_HANDLE_PREF = "key_handle"
    }

    private lateinit var navController: NavHostController
    private var pendingUserName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Fido2MobileAppTheme {
                navController = rememberNavController()
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController)
                    }
                    composable("register") {
                        RegisterScreen(
                            navController,
                            showFidoDialog = this@MainActivity::showFidoDialog
                        )
                    }
                    composable("login") {
                        LoginScreen(
                            navController,
                            showAuthDialog = this@MainActivity::showAuthDialog
                        )
                    }
                    composable(
                        route = "profile/{userName}",
                        arguments = listOf(
                            navArgument("userName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        // Retrieve username from nav arguments
                        val userName = backStackEntry.arguments?.getString("userName") ?: ""
                        ProfileScreen(
                            navController = navController,
                            userName = userName,
                            onLogout = {
                                // Example logout logic: navigate to login
                                navController.navigate("login") {
                                    popUpTo("home") { inclusive = false }
                                }
                            }
                        )
                    }
                }
            }
        }

        notificationPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    showPermissionSettingsDialog("Notification")
                }
            }

        cameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (!isGranted) {
                    showPermissionSettingsDialog("Camera")
                }
            }
        checkNotificationPermission()
        val notificationSettings = isNotificationEnabled()
        val util = Util()
        Log.i(
            LOG_TAG, "getAppSigningHash ${
                util.getAppSigningHash(
                    packageManager.getPackageInfo(
                        packageName,
                        PackageManager.GET_SIGNATURES
                    )
                )
            }"
        )
    }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 100) {
//            if (resultCode == RESULT_OK && data != null) {
//                // Handle FIDO2 response
//                val responseData = data.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
//                if (responseData != null) {
//                    Log.d("FIDO2", "Registration success! onActivityResult Credential received. $responseData")
//                    val credential = com.google.android.gms.fido.fido2.api.common.PublicKeyCredential.deserializeFromBytes(responseData)
//                    val response = credential.response
//                    if(response is AuthenticatorErrorResponse) {
//                        Log.d("FIDO2", "Registration error. $response")
//                    } else {
//                        handleRegisterResponse(response as AuthenticatorAttestationResponse)
//                    }
//                }
//            } else {
//                Log.e("FIDO2", "Registration failed or cancelled")
//            }
//        }
//    }

    private val fido2RegisterLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // Handle the FIDO2 response
                val responseData = data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
                if (responseData != null) {
                    Log.d(
                        "FIDO2",
                        "Registration success! fido2RegisterLauncher Credential received. $responseData"
                    )
                    val credential = PublicKeyCredential.deserializeFromBytes(
                        responseData
                    )
                    val response = credential.response
                    if (response is AuthenticatorErrorResponse) {
                        Log.e("FIDO2", "Registration error. $response")
                        val errorMessage = Fido2Utils.getFido2ErrorMessage(response)
                        runOnUiThread {
                            setRegistrationError("Registration failed: $errorMessage")
                            Toast.makeText(
                                this@MainActivity,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        pendingUserName = null
                    } else {
                        // Use the stored username
                        pendingUserName?.let { userName ->
                            handleRegisterResponse(userName, credential)
                        }
                        pendingUserName = null
                    }
                } else {
                    Log.e("FIDO2", "Registration canceled or failed")
                    pendingUserName = null
                }
            }
        }

    // Accept userName as parameter
    fun showFidoDialog(userName: String, response: String) {
        Log.i("Response", "Json response $response")
        pendingUserName = userName
        val publicKeyCredOpt = Fido2OptionDataBuilder.prepareRegistrationOptions(response)

        val fido2Client = Fido.getFido2ApiClient(this)
        val fido2PendingIntentTask =
            fido2Client.getRegisterPendingIntent(publicKeyCredOpt)
        fido2PendingIntentTask.addOnSuccessListener { fido2PendingIntent ->
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(fido2PendingIntent).build()
                fido2RegisterLauncher.launch(intentSenderRequest)
            } catch (e: IntentSender.SendIntentException) {
                Log.e("Fido2RegistrationHelper", "Error starting FIDO2 PendingIntent", e)
                pendingUserName = null
            }
        }.addOnFailureListener {
            Log.e("Fido2RegistrationHelper", "Failed to launch FIDO2 intent", it)
            pendingUserName = null
        }
    }

    /**
     * The response should be sent to the Relying Party / server to validate and store
     */
    fun handleRegisterResponse(userName: String, response: PublicKeyCredential) {

//        val keyHandleBase64 = Base64.encodeToString(response.rawId, Base64.DEFAULT)
//        val clientDataJson = String(response.response.clientDataJSON, Charsets.UTF_8)
////        val attestationObjectBase64 =
////            Base64.encodeToString(response., Base64.DEFAULT)
//
//
//        Log.d(LOG_TAG, "keyHandleBase64: $keyHandleBase64")
//        Log.d(LOG_TAG, "clientDataJSON: $clientDataJson")
////        Log.d(LOG_TAG, "attestationObjectBase64: $attestationObjectBase64")
//
//        val registerFido2Result = "Authenticator Attestation Response\n\n" +
//                "keyHandleBase64:\n" +
//                "$keyHandleBase64\n\n" +
//                "clientDataJSON:\n" +
//                "$clientDataJson\n\n" +
//                "attestationObjectBase64:\n" +
//                "$attestationObjectBase64\n"
        Log.i(LOG_TAG, "RegisterFido2Result $response")

        // Send to server
        sendRegisterResponseToServer(
            userName,
            response
        )
    }

    /**
     * Sends the FIDO2 registration response to the server via POST.
     */
    private fun sendRegisterResponseToServer(
        userName: String,
        response: PublicKeyCredential
    ) {
        val params = mapOf(
            "clientDataJSON" to response.toJson(),
            "attestationObject" to response.toJson()
        )

        val url = Constants.BASE_URL + Constants.FIDO2_REGISTER_COMPLETE_PATH
        val encodedUsername = URLEncoder.encode(userName, "UTF-8")
        val fullUrl = "$url?username=$encodedUsername"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiService.postApi(fullUrl, params, { success, apiResponse ->
                    try {
                        if (success && apiResponse != null) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Registration Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // Navigate to login or profile page on success
                                navController.navigate("login")
                            }
                        } else {
                            runOnUiThread {
                                // Instead of just showing a toast, we can set a registration error state
                                // that will be observed by the RegisterScreen
                                setRegistrationError("Registration failed. The operation was cancelled or not allowed.")
                            }
                            Log.e("TokenExchange", "Failed to register user $apiResponse")
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            setRegistrationError("Registration Failed: ${e.message}")
                        }
                        Log.e("Register", "Register device failed : ${e.message}")
                    }
                })
                Log.i(LOG_TAG, "FIDO2 registration sent to backend")
            } catch (e: Exception) {
                runOnUiThread {
                    setRegistrationError("Network Error: ${e.message}")
                }
                Log.e(LOG_TAG, "Error sending FIDO2 registration", e)
            }
        }
    }

    // Variable to hold registration error messages
    private var _registrationError = mutableStateOf<String?>(null)
    val registrationError: State<String?> = _registrationError

    fun setRegistrationError(errorMessage: String?) {
        _registrationError.value = errorMessage
    }

    fun clearRegistrationError() {
        _registrationError.value = null
    }

    // Add state for authentication errors
    private var _authenticationError = mutableStateOf<String?>(null)
    val authenticationError: State<String?> = _authenticationError

    fun setAuthenticationError(errorMessage: String?) {
        _authenticationError.value = errorMessage
    }

    fun clearAuthenticationError() {
        _authenticationError.value = null
    }

    private fun showPermissionSettingsDialog(permissionName: String) {
        AlertDialog.Builder(this)
            .setTitle("$permissionName Permission Required")
            .setMessage("Please enable $permissionName permission in settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // In your Activity
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Handle the permission result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(
                        this,
                        "Notification permission required for authentication",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    fun isNotificationEnabled(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManagerCompat.from(this).areNotificationsEnabled()
        } else {
            true
        }
    }

    fun handleAuthorizationResponse(intent: Intent?) {
        val data = intent?.data
        if (data != null && data.toString().startsWith("com.konasl.fido2mobileapp:/callback")) {
            val authCode = data.getQueryParameter("code")
            val state = data.getQueryParameter("state")
            println("Authorization Code: $authCode")
            println("State: $state")
            // TODO: Exchange code for tokens or initiate WebAuthn registration
        }
    }

    private val fido2AuthLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                // Handle the FIDO2 response
                val responseData = data?.getByteArrayExtra(Fido.FIDO2_KEY_CREDENTIAL_EXTRA)
                if (responseData != null) {
                    Log.d(
                        "FIDO2",
                        "Authentication response received. $responseData"
                    )
                    val credential = PublicKeyCredential.deserializeFromBytes(
                        responseData
                    )
                    val response = credential.response
                    if (response is AuthenticatorErrorResponse) {
                        Log.e("FIDO2", "Authentication error. $response")
                        val errorMessage = Fido2Utils.getFido2ErrorMessage(response)
                        runOnUiThread {
                            setAuthenticationError("Authentication failed: $errorMessage")
                            Toast.makeText(
                                this@MainActivity,
                                "Authentication failed: $errorMessage",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        pendingUserName = null
                    } else {
                        // Use the stored username
                        pendingUserName?.let { userName ->
                            sendAuthResponseToServer(userName, credential)
                        }
                        pendingUserName = null
                    }
                } else {
                    Log.e("FIDO2", "Authentication canceled or failed")
                    setAuthenticationError("Authentication failed. The operation was cancelled or not allowed.")
                    pendingUserName = null
                }
            } else {
                setAuthenticationError("Authentication failed. The operation was cancelled or not allowed.")
            }
        }

    fun showAuthDialog(userName: String, response: String) {
        Log.i("Response", "Json response $response")
        pendingUserName = userName
        val publicKeyCredOpt = Fido2OptionDataBuilder.prepareAuthenticationOptions(response)

        val fido2Client = Fido.getFido2ApiClient(this)
        val fido2PendingIntentTask =
            fido2Client.getSignPendingIntent(publicKeyCredOpt)
        fido2PendingIntentTask.addOnSuccessListener { fido2PendingIntent ->
            try {
                val intentSenderRequest = IntentSenderRequest.Builder(fido2PendingIntent).build()
                fido2AuthLauncher.launch(intentSenderRequest)
            } catch (e: IntentSender.SendIntentException) {
                Log.e("Fido2AuthHelper", "Error starting FIDO2 PendingIntent", e)
                pendingUserName = null
            }
        }.addOnFailureListener {
            Log.e("Fido2AuthHelper", "Failed to launch FIDO2 intent", it)
            pendingUserName = null
        }
    }


    /**
     * Sends the FIDO2 authentication response to the server via POST.
     */
    private fun sendAuthResponseToServer(
        userName: String,
        response: PublicKeyCredential
    ) {
        val params = mapOf(
            "clientDataJSON" to response.toJson(),
            "attestationObject" to response.toJson()
        )

        val url = Constants.BASE_URL + Constants.FIDO2_AUTHENTICATION_COMPLETE_PATH
        val encodedUsername = URLEncoder.encode(userName, "UTF-8")
        val fullUrl = "$url?username=$encodedUsername"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiService.postApi(fullUrl, params, { success, apiResponse ->
                    Log.i("AuthResponse", "Auth response from server ${success}, $apiResponse")
                    try {
                        if (success && apiResponse != null) {
                            runOnUiThread {
                                clearAuthenticationError() // Clear any previous errors
                                Toast.makeText(
                                    this@MainActivity,
                                    "Authentication Successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            showProfile(userName)
                        } else {
                            runOnUiThread {
                                setAuthenticationError("Authentication failed. The operation was cancelled or not allowed.")
                                Toast.makeText(
                                    this@MainActivity,
                                    "Authentication Failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            Log.e("TokenExchange", "Failed to authenticate user $apiResponse")
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            setAuthenticationError("Authentication Failed: ${e.message}")
                            Toast.makeText(
                                this@MainActivity,
                                "Authentication Failed: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        Log.e("Authentication", "User authentication failed: ${e.message}")
                    }
                })
                Log.i(LOG_TAG, "FIDO2 authentication data sent to backend")
            } catch (e: Exception) {
                runOnUiThread {
                    setAuthenticationError("Network Error: ${e.message}")
                }
                Log.e(LOG_TAG, "Error sending FIDO2 authentication", e)
            }
        }
    }


    fun showProfile(userName: String) {
        runOnUiThread {
            // Update to use the new route format with userName as a path parameter        }
            navController.navigate("profile/${Uri.encode(userName)}")   
        }
    }

    fun handleLogout(userName: String, response: String) {
        Log.i("Response", "Json response $response")
        runOnUiThread {
            // Navigate back to login screen
            navController.navigate("login")
        }
    }

    /**
     * The response should be sent to the Relying Party / server to validate
     */
    private fun handleSignResponse(fido2Response: ByteArray) {
        val response = AuthenticatorAssertionResponse.deserializeFromBytes(fido2Response)
        val keyHandleBase64 = Base64.encodeToString(response.keyHandle, Base64.DEFAULT)
        val clientDataJson = String(response.clientDataJSON, Charsets.UTF_8)
        val authenticatorDataBase64 =
            Base64.encodeToString(response.authenticatorData, Base64.DEFAULT)
        val signatureBase64 = Base64.encodeToString(response.signature, Base64.DEFAULT)

        Log.d(LOG_TAG, "keyHandleBase64: $keyHandleBase64")
        Log.d(LOG_TAG, "clientDataJSON: $clientDataJson")
        Log.d(LOG_TAG, "authenticatorDataBase64: $authenticatorDataBase64")
        Log.d(LOG_TAG, "signatureBase64: $signatureBase64")

        val signFido2Result = "Authenticator Assertion Response\n\n" +
                "keyHandleBase64:\n" +
                "$keyHandleBase64\n\n" +
                "clientDataJSON:\n" +
                "$clientDataJson\n\n" +
                "authenticatorDataBase64:\n" +
                "$authenticatorDataBase64\n\n" +
                "signatureBase64:\n" +
                "$signatureBase64\n"
        Log.i(LOG_TAG, "handleSignResponse $signFido2Result")
//        resultText.text = signFido2Result
    }

    // private fun loadKeyHandle(): ByteArray? {
    //     val keyHandleBase64 = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_HANDLE_PREF, null)
    //         ?: return null
    //     return Base64.decode(keyHandleBase64, Base64.DEFAULT)
    // }

//    private fun storeKeyHandle(keyHandle: ByteArray) {
//        PreferenceManager.getDefaultSharedPreferences(this).edit {
//            putString(KEY_HANDLE_PREF, Base64.encodeToString(keyHandle, Base64.DEFAULT))
//        }
//    }

}