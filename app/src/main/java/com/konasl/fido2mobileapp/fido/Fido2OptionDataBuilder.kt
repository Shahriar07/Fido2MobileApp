package com.konasl.fido2mobileapp.fido

import android.util.Base64
import android.util.Log
import com.google.android.gms.fido.fido2.api.common.Attachment
import com.google.android.gms.fido.fido2.api.common.AttestationConveyancePreference
import com.google.android.gms.fido.fido2.api.common.AuthenticatorSelectionCriteria
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialParameters
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRequestOptions
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialRpEntity
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialUserEntity
import com.google.android.gms.fido.fido2.api.common.ResidentKeyRequirement
import com.konasl.fido2mobileapp.utils.WebAuthnJsonParser

object Fido2OptionDataBuilder {

    fun ByteArray.toHexString(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    fun prepareRegistrationOptions(optionsJson: String): PublicKeyCredentialCreationOptions {
        try {
//            val json = JSONObject(optionsJson)
//            val options = PublicKeyCredentialCreationOptions(optionsJson);
            val parser = WebAuthnJsonParser();
            val credentialOptions = parser.parseFromJson(optionsJson)
            val userIdBytes =
                Base64.decode(
                    credentialOptions.user.id,
                    Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
                )
            val challengeBytes = Base64.decode(credentialOptions.challenge.value, Base64.DEFAULT)
            Log.i(
                "prepareRegistrationOptions",
                "challengeBytes HexString ${challengeBytes.toHexString()}"
            )
            Log.i(
                "prepareRegistrationOptions",
                "userIdBytes HexString ${userIdBytes.toHexString()}"
            )
            val publicKeyCredentialCreationOptions = PublicKeyCredentialCreationOptions.Builder()
                .setRp(
                    PublicKeyCredentialRpEntity(
                        credentialOptions.relyingParty.id,
                        credentialOptions.relyingParty.name,
                        null
                    )
                )
                .setUser(
                    PublicKeyCredentialUserEntity(
                        userIdBytes,
                        credentialOptions.user.name,
                        null,
                        credentialOptions.user.displayName
                    )
                )
                .setChallenge(challengeBytes)
                .setParameters(
                    credentialOptions.publicKeyCredentialParameters.map { param ->
                        PublicKeyCredentialParameters(
                            param.type,
                            param.algorithm
                        )
                    }
                )
                .setAuthenticatorSelection(
                    AuthenticatorSelectionCriteria.Builder()
                        .setAttachment(Attachment.CROSS_PLATFORM) // Secure element
                        .setRequireResidentKey(false)
                        .setResidentKeyRequirement(ResidentKeyRequirement.RESIDENT_KEY_PREFERRED)
//                        .setUserVerification(UserVerificationRequirement.PREFERRED)
                        .build()
                )
                .setTimeoutSeconds(credentialOptions.timeout.toDouble())
                .setExcludeList(emptyList())
                .setAttestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                .build()

            return publicKeyCredentialCreationOptions;

        } catch (e: Exception) {
            Log.e("Fido2RegistrationHelper", "Error parsing creation options", e)

        }
        return TODO("Provide the return value")
    }


    fun prepareAuthenticationOptions(optionsJson: String): PublicKeyCredentialRequestOptions {
        try {
            val parser = WebAuthnJsonParser();
            val credentialOptions = parser.parseRequestFromJson(optionsJson)
            val challengeBytes = Base64.decode(credentialOptions.challenge.value, Base64.DEFAULT)
            Log.i(
                "prepareRegistrationOptions",
                "challengeBytes HexString ${challengeBytes.toHexString()}"
            )

            val publicKeyCredentialRequestOptions = PublicKeyCredentialRequestOptions.Builder()
                .setRpId(credentialOptions.rpId)
                .setChallenge(challengeBytes)
                .setAllowList(credentialOptions.allowCredentials)
                .zzc(credentialOptions.userVerification)
                .setTimeoutSeconds(credentialOptions.timeout.toDouble())
                .build()

            return publicKeyCredentialRequestOptions;

        } catch (e: Exception) {
            Log.e("Fido2RegistrationHelper", "Error parsing creation options", e)
            return PublicKeyCredentialRequestOptions.Builder().build();

        }
        return TODO("Provide the return value")
    }
}
