//package com.konasl.fido2mobileapp.fido
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.util.Base64
//import android.util.Log
//import com.google.android.gms.fido.Fido
//import com.google.android.gms.fido.fido2.api.common.*
//import kotlinx.coroutines.tasks.await
//
///**
// * Manager class for handling FIDO2 operations with secure element
// */
//class FIDO2Manager(private val context: Context) {
//    private val tag = "FIDO2Manager"
//    private val fido2ApiClient = Fido.getFido2ApiClient(context)
//
//    interface FIDO2OperationListener {
//        fun onSuccess(credentialId: String, response: ByteArray, type: OperationType)
//        fun onError(error: Exception)
//    }
//
//    enum class OperationType {
//        REGISTRATION,
//        AUTHENTICATION
//    }
//
//    /**
//     * Register a new FIDO2 credential
//     */
//    suspend fun registerFidoCredential(
//        username: String,
//        displayName: String,
//        rpId: String,
//        rpName: String,
//        challenge: ByteArray
//    ): Result<PublicKeyCredential> {
//        return try {
//            Log.d(tag, "Starting FIDO2 registration for user: $username")
//
//            val userHandle = username.toByteArray()
//
//            val publicKeyCredentialCreationOptions = PublicKeyCredentialCreationOptions.Builder()
//                .setRp(PublicKeyCredentialRpEntity(rpId, rpName))
//                .setUser(PublicKeyCredentialUserEntity(userHandle, username, displayName))
//                .setChallenge(challenge)
//                .setParameters(listOf(
//                    PublicKeyCredentialParameters(
//                        PublicKeyCredentialType.PUBLIC_KEY.toString(),
//                        EC2Algorithm.ES256.algoValue
//                    ),
//                    PublicKeyCredentialParameters(
//                        PublicKeyCredentialType.PUBLIC_KEY.toString(),
//                        RSAAlgorithm.RS256.algoValue
//                    )
//                ))
//                .setAuthenticatorSelection(
//                    AuthenticatorSelectionCriteria.Builder()
//                        .setAttachment(AuthenticatorAttachment.PLATFORM) // Secure element
//                        .setRequireResidentKey(true)
//                        .setUserVerification(UserVerificationRequirement.PREFERRED)
//                        .build()
//                )
//                .setAttestationConveyancePreference(AttestationConveyancePreference.DIRECT)
//                .build()
//
//            // Get the registration pending intent
//            val pendingIntent = fido2ApiClient.getRegisterPendingIntent(publicKeyCredentialCreationOptions).await()
//
//            // This pending intent would be handled in the calling activity
//            Result.success(PublicKeyCredential(pendingIntent, publicKeyCredentialCreationOptions))
//        } catch (e: Exception) {
//            Log.e(tag, "Error in FIDO2 registration", e)
//            Result.failure(e)
//        }
//    }
//
//    /**
//     * Authenticate with a FIDO2 credential
//     */
//    suspend fun authenticateWithFido(
//        rpId: String,
//        challenge: ByteArray,
//        credentialIds: List<ByteArray>
//    ): Result<PublicKeyCredential> {
//        return try {
//            Log.d(tag, "Starting FIDO2 authentication")
//
//            val allowCredentials = credentialIds.map { id ->
//                PublicKeyCredentialDescriptor(
//                    PublicKeyCredentialType.PUBLIC_KEY.toString(),
//                    id,
//                    null
//                )
//            }
//
//            val publicKeyCredentialRequestOptions = PublicKeyCredentialRequestOptions.Builder()
//                .setRpId(rpId)
//                .setChallenge(challenge)
//                .setAllowList(allowCredentials)
//                .setUserVerification(UserVerificationRequirement.PREFERRED)
//                .setTimeoutSeconds(60.0)
//                .build()
//
//            // Get the authentication pending intent
//            val pendingIntent = fido2ApiClient.getSignPendingIntent(publicKeyCredentialRequestOptions).await()
//
//            // This pending intent would be handled in the calling activity
//            Result.success(PublicKeyCredential(pendingIntent, publicKeyCredentialRequestOptions))
//        } catch (e: Exception) {
//            Log.e(tag, "Error in FIDO2 authentication", e)
//            Result.failure(e)
//        }
//    }
//
//    /**
//     * Handle the result from FIDO2 operation
//     */
//    suspend fun handleFido2Result(resultCode: Int, data: Intent?, type: OperationType): Result<AuthenticatorResponse> {
//        return try {
//            Log.d(tag, "Processing FIDO2 result, type: $type")
//
//            if (resultCode != Activity.RESULT_OK || data == null) {
//                return Result.failure(Exception("FIDO2 operation canceled or failed"))
//            }
//
//            val response = when (type) {
//                OperationType.REGISTRATION -> {
//                    fido2ApiClient.getFido2RegistrationResponse(data).await()
//                }
//                OperationType.AUTHENTICATION -> {
//                    fido2ApiClient.getFido2SignResponse(data).await()
//                }
//            }
//
//            Result.success(response)
//        } catch (e: Exception) {
//            Log.e(tag, "Error processing FIDO2 result", e)
//            Result.failure(e)
//        }
//    }
//
//    /**
//     * Extract registration data for sending to server
//     */
//    fun extractRegistrationData(response: AuthenticatorAttestationResponse): Map<String, String> {
//        return mapOf(
//            "id" to Base64.encodeToString(response.keyHandle, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "rawId" to Base64.encodeToString(response.keyHandle, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "type" to "public-key",
//            "clientDataJSON" to Base64.encodeToString(response.clientDataJSON, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "attestationObject" to Base64.encodeToString(response.attestationObject, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
//        )
//    }
//
//    /**
//     * Extract authentication data for sending to server
//     */
//    fun extractAuthenticationData(response: AuthenticatorAssertionResponse): Map<String, String> {
//        val data = mapOf(
//            "id" to Base64.encodeToString(response.keyHandle, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "rawId" to Base64.encodeToString(response.keyHandle, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "type" to "public-key",
//            "authenticatorData" to Base64.encodeToString(response.authenticatorData, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "clientDataJSON" to Base64.encodeToString(response.clientDataJSON, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP),
//            "signature" to Base64.encodeToString(response.signature, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
//        )
//
//        // Add userHandle if available
//        response.userHandle?.let {
//            data.plus("userHandle" to Base64.encodeToString(it, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP))
//        }
//
//        return data
//    }
//
    data class PublicKeyCredential(
        val pendingIntent: android.app.PendingIntent,
        val options: Any
    )
//}
