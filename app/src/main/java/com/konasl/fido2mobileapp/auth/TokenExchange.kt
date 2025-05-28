package com.konasl.fido2mobileapp.auth

import android.util.Log
import com.konasl.fido2mobileapp.network.ApiService
import org.json.JSONObject

object TokenExchange {

    fun exchangeAuthCodeForToken(
        authCode: String,
        redirectUri: String,
        onResult: (accessToken: String?, idToken: String?) -> Unit
    ) {
        val url = "https://dev-12202026.okta.com/oauth2/v1/token"
        val formParams = mapOf(
            "grant_type" to "authorization_code",
            "client_id" to "0oaowg1kucupMxmmY5d7",
            "redirect_uri" to redirectUri,
            "code" to authCode,
            "code_verifier" to "my-code-verifier" // replace with real verifier if using PKCE
        )

        ApiService.postForm(url, formParams) { success, response ->
            if (success && response != null) {
                val json = JSONObject(response)
                val accessToken = json.optString("access_token")
                val idToken = json.optString("id_token")
                onResult(accessToken, idToken)
            } else {
                Log.e("TokenExchange", "Failed to exchange token")
                onResult(null, null)
            }
        }
    }
}
