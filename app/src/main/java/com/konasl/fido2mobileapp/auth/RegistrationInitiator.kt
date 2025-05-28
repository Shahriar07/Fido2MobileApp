package com.konasl.fido2mobileapp.auth

import android.util.Log
import com.konasl.fido2mobileapp.network.ApiService
import org.json.JSONObject

object RegistrationInitiator {

    fun getRegistrationOptions(
        username: String,
        accessToken: String,
        onResult: (creationOptionsJSON: String?) -> Unit
    ) {
        val url = "https://dev-kidp.konancp.com/register/options?username=$username"
        val headers = mapOf(
            "Authorization" to "Bearer $accessToken"
        )

        ApiService.get(url, headers) { success, response ->
            if (success && response != null) {
                onResult(response)
            } else {
                Log.e("RegistrationInitiator", "Failed to fetch registration options")
                onResult(null)
            }
        }
    }
}
