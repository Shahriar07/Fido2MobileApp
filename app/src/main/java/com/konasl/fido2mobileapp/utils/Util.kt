package com.konasl.fido2mobileapp.utils

import android.content.pm.PackageInfo
import android.util.Base64
import android.util.Log
import androidx.core.content.PackageManagerCompat.LOG_TAG
import com.konasl.fido2mobileapp.fido.Fido2OptionDataBuilder.toHexString
import java.security.MessageDigest

class Util {

    fun  getAppSigningHash(packageInfo: PackageInfo): String {
        try {
            val signature = packageInfo.signatures?.get(0)
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(signature?.toByteArray())
            Log.i(LOG_TAG, "Digest ${digest.toHexString()}")
            return Base64.encodeToString(digest, Base64.NO_WRAP or Base64.URL_SAFE)
                .replace("=", "")
        } catch (e: Exception) {
            return ""
        }
    }
}