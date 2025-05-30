package com.konasl.fido2mobileapp.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.EditText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.konasl.fido2mobileapp.pkce.PkceUtil
import com.konasl.fido2mobileapp.utils.Constants
import androidx.core.net.toUri
import com.konasl.fido2mobileapp.network.ApiService
import org.json.JSONObject

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val (username, setUserName) = remember { mutableStateOf("") }
    val ( display, setDisplay) = remember { mutableStateOf("") }
    val ( credname, setCredname) = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Begin registration using your Secure Element (NFC key)")

        Spacer(modifier = Modifier.height(16.dp))
        // Input field below the text

        OutlinedTextField(
            value = username,
            onValueChange = { setUserName(it) },
            label = { Text("Enter User Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = display,
            onValueChange = { setDisplay(it) },
            label = { Text("Enter Display Name") },
            modifier = Modifier.fillMaxWidth()
        )
//        Spacer(modifier = Modifier.height(16.dp))
//        OutlinedTextField(
//            value = credname,
//            onValueChange = { setCredname(it) },
//            label = { Text("Enter Credential Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {

                val url = "https://dev-kidp.konancp.com/webauthn/register/options"
                val formParams = mapOf(
                    "username" to username,
                    "displayName" to display
                )

                ApiService.postForm(url, formParams) { success, response ->
                    if (success && response != null) {
                        val json = JSONObject(response)
                        Log.i("Response", "Json response $json")
//                        val accessToken = json.optString("access_token")
//                        val idToken = json.optString("id_token")
//                        onResult(accessToken, idToken)
                    } else {
                        Log.e("TokenExchange", "Failed to exchange token")
//                        onResult(null, null)
                    }
                }



//
//            val verifier = PkceUtil.generateCodeVerifier()
//            val challenge = PkceUtil.generateCodeChallenge(verifier)
//            setCodeVerifier(verifier)
//
//            val authUri = Constants.AUTHORIZATION_ENDPOINT.toUri().buildUpon()
//                .appendQueryParameter("idp", Constants.IDP_ID)
//                .appendQueryParameter("client_id", Constants.CLIENT_ID)
//                .appendQueryParameter("response_type", Constants.RESPONSE_TYPE)
//                .appendQueryParameter("scope", Constants.SCOPE)
//                .appendQueryParameter("redirect_uri", Constants.REDIRECT_URI)
//                .appendQueryParameter("state", Constants.STATE)
//                .appendQueryParameter("nonce", Constants.NONCE)
//                .appendQueryParameter("code_challenge", challenge)
//                .appendQueryParameter("code_challenge_method", "S256")
//                .appendQueryParameter("screen_hint", "signup")
//                .build()
//
//            val browserIntent = Intent(Intent.ACTION_VIEW, authUri)
//            context.startActivity(browserIntent)

        }) {
            Text("Register via IdP")
        }
    }
}
