package com.konasl.fido2mobileapp.utils

import com.google.android.gms.fido.common.Transport
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor
import com.google.gson.Gson
import com.konasl.fido2mobileapp.auth.WebAuthnCredentialCreationOptions
import com.konasl.fido2mobileapp.auth.WebAuthnCredentialRequestOptions
import org.json.JSONArray

class WebAuthnJsonParser {
    private val gson = Gson()

    /**
     * Converts a JSON string to WebAuthnCredentialCreationOptions object
     * @param jsonString The JSON string to parse
     * @return WebAuthnCredentialCreationOptions object
     * @throws Exception if parsing fails
     */
    fun parseFromJson(jsonString: String): WebAuthnCredentialCreationOptions {
        return try {
            gson.fromJson(jsonString, WebAuthnCredentialCreationOptions::class.java)
        } catch (e: Exception) {
            throw Exception("Failed to parse WebAuthn JSON: ${e.message}", e)
        }
    }

    /**
     * Converts a JSON string to WebAuthnCredentialRequestOptions object
     * @param jsonString The JSON string to parse
     * @return WebAuthnCredentialRequestOptions object
     * @throws Exception if parsing fails
     */
    fun parseRequestFromJson(jsonString: String): WebAuthnCredentialRequestOptions {
        try {
            val jsonObj = com.google.gson.JsonParser.parseString(jsonString).asJsonObject
            val allowCredentialsArray = jsonObj.getAsJsonArray("allowCredentials")
            val allowedCreds = if (allowCredentialsArray != null) {
                ArrayList<PublicKeyCredentialDescriptor>(allowCredentialsArray.size())
            } else {
                ArrayList<PublicKeyCredentialDescriptor>()
            }
            if (allowCredentialsArray != null) {
                for (credElement in allowCredentialsArray) {
                    val credObj = credElement.asJsonObject
                    val idBase64 = credObj.get("id")?.asString
                    if (idBase64 != null) {
                        val idBytes = try {
                            android.util.Base64.decode(idBase64, android.util.Base64.URL_SAFE or android.util.Base64.NO_PADDING or android.util.Base64.NO_WRAP)
                        } catch (e: IllegalArgumentException) {
                            android.util.Base64.decode(idBase64, android.util.Base64.DEFAULT)
                        }
                        // Get transports as JSONArray for parseTransports
                        val transportsJsonArray = if (credObj.has("transports") && credObj.get("transports").isJsonArray) {
                            val gsonArray = credObj.getAsJsonArray("transports")
                            val jsonArray = JSONArray()
                            for (t in gsonArray) {
                                jsonArray.put(t.asString)
                            }
                            jsonArray
                        } else {
                            JSONArray()
                        }
                        allowedCreds.add(
                            PublicKeyCredentialDescriptor(
                                credObj.get("type").asString,
                                idBytes,
                                Transport.parseTransports(transportsJsonArray)
                            )
                        )
                    }
                }
            }
            // Now parse the possibly modified JSON to the data class
            var credentialRequestOptions = gson.fromJson(jsonObj, WebAuthnCredentialRequestOptions::class.java)
            credentialRequestOptions.allowCredentials = allowedCreds
            return credentialRequestOptions
        } catch (e: Exception) {
            throw Exception("Failed to parse WebAuthn JSON: ${e.message}", e)
        }
    }


    /**
     * Converts WebAuthnCredentialCreationOptions object back to JSON string
     * @param options The WebAuthnCredentialCreationOptions object
     * @return JSON string representation
     */
    fun toJson(options: WebAuthnCredentialCreationOptions): String {
        return gson.toJson(options)
    }
}