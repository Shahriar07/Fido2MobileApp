package com.konasl.fido2mobileapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val (codeVerifier, setCodeVerifier) = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Begin registration using your Secure Element (NFC key)")
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val verifier = PkceUtil.generateCodeVerifier()
            val challenge = PkceUtil.generateCodeChallenge(verifier)
            setCodeVerifier(verifier)

            val authUri = Uri.parse(Constants.AUTHORIZATION_ENDPOINT).buildUpon()
                .appendQueryParameter("idp", Constants.IDP_ID)
                .appendQueryParameter("client_id", Constants.CLIENT_ID)
                .appendQueryParameter("response_type", Constants.RESPONSE_TYPE)
                .appendQueryParameter("scope", Constants.SCOPE)
                .appendQueryParameter("redirect_uri", Constants.REDIRECT_URI)
                .appendQueryParameter("state", Constants.STATE)
                .appendQueryParameter("nonce", Constants.NONCE)
                .appendQueryParameter("code_challenge", challenge)
                .appendQueryParameter("code_challenge_method", "S256")
                .build()

            val browserIntent = Intent(Intent.ACTION_VIEW, authUri)
            context.startActivity(browserIntent)

        }) {
            Text("Register via IdP")
        }
    }
}
