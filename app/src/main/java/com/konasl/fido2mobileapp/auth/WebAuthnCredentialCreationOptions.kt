package com.konasl.fido2mobileapp.auth;

import com.google.gson.annotations.SerializedName

// Data classes to represent the JSON structure
data class WebAuthnCredentialCreationOptions(
        @SerializedName("rp")
        val relyingParty: RelyingParty,

        @SerializedName("user")
        val user: User,

        @SerializedName("challenge")
        val challenge: Challenge,

        @SerializedName("pubKeyCredParams")
        val publicKeyCredentialParameters: List<PublicKeyCredentialParameters>,

        @SerializedName("timeout")
        val timeout: Long,

        @SerializedName("excludeCredentials")
        val excludeCredentials: List<Any>? = null,

        @SerializedName("authenticatorSelection")
        val authenticatorSelection: AuthenticatorSelection,

        @SerializedName("hints")
        val hints: List<Any>? = null,

        @SerializedName("attestation")
        val attestation: String,

        @SerializedName("extensions")
        val extensions: Any? = null
)

data class RelyingParty(
        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        val name: String
)

data class User(
        @SerializedName("id")
        val id: String,

        @SerializedName("name")
        val name: String,

        @SerializedName("displayName")
        val displayName: String
)

data class Challenge(
        @SerializedName("value")
        val value: String
)

data class PublicKeyCredentialParameters(
        @SerializedName("type")
        val type: String,

        @SerializedName("alg")
        val algorithm: Int
)

data class AuthenticatorSelection(
        @SerializedName("authenticatorAttachment")
        val authenticatorAttachment: String,

        @SerializedName("requireResidentKey")
        val requireResidentKey: Boolean,

        @SerializedName("residentKey")
        val residentKey: String? = null,

        @SerializedName("userVerification")
        val userVerification: String
)