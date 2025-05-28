package com.konasl.fido2mobileapp.fido

import android.app.Activity
import android.util.Log
import com.google.android.gms.fido.Fido
import com.google.android.gms.fido.fido2.api.common.PublicKeyCredentialCreationOptions
import org.json.JSONObject

object Fido2RegistrationHelper {

    private const val REQUEST_CODE_REGISTER = 1001

    fun startRegistration(activity: Activity, optionsJson: String) {
        try {
            val json = JSONObject(optionsJson)
            val options = PublicKeyCredentialCreationOptions.deserializeFromBytes(
                optionsJson.toByteArray()
            )

            val fido2Client = Fido.getFido2ApiClient(activity)
            val fido2PendingIntentTask = fido2Client.getRegisterPendingIntent(options)

            fido2PendingIntentTask.addOnSuccessListener { fido2PendingIntent ->
                activity.startIntentSenderForResult(
                    fido2PendingIntent.intentSender,
                    REQUEST_CODE_REGISTER,
                    null,
                    0,
                    0,
                    0
                )
            }.addOnFailureListener {
                Log.e("Fido2RegistrationHelper", "Failed to launch FIDO2 intent", it)
            }

        } catch (e: Exception) {
            Log.e("Fido2RegistrationHelper", "Error parsing creation options", e)
        }
    }
}
