package com.konasl.fido2mobileapp.auth

import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialDescriptor
import com.google.android.gms.fido.fido2.api.common.UserVerificationRequirement
import com.google.gson.annotations.SerializedName

// Data classes to represent the JSON structure
data class WebAuthnCredentialRequestOptions(
    @SerializedName("challenge")
    val challenge: Challenge,

    @SerializedName("timeout")
 val timeout: Long,

@SerializedName("rpId")
 val rpId: String,

@SerializedName("allowCredentials")
    var allowCredentials: ArrayList<PublicKeyCredentialDescriptor>,

@SerializedName("userVerification")
 val userVerification: UserVerificationRequirement,

//@SerializedName("hints")
// val hints: MutableList<PublicKeyCredentialHints?>,

//@SerializedName("extensions")
// val extensions: AuthenticationExtensionsClientInputs<AuthenticationExtensionClientInput?>
)