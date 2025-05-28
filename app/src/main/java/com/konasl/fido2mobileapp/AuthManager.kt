package com.konasl.fido2mobileapp

import android.app.Activity
import android.content.Context
import android.net.Uri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class AuthManager(private val context: Context) {

    private val clientId = "0oaowg1kucupMxmmY5d7"
    private val clientSecret = "_ps517Ql4J5ec1eJmuWT-VoO_YFfRByS0N9ur0nZFTWdz4yMV_p-TmVXimcMmp5X"

    //    private val redirectUri = "com.konasl.fido2mobileapp:/callback"
    private val redirectUri = "com.okta.trial-5309856:/callback"
    private val oktaDomain = "https://trial-5309856.okta.com"

    private val serviceConfig = AuthorizationServiceConfiguration(
        Uri.parse("$oktaDomain/oauth2/default/v1/authorize"),
        Uri.parse("$oktaDomain/oauth2/default/v1/token")
    )

    fun startLogin(activity: Activity) {
        val authRequest = AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            Uri.parse(redirectUri)
        ).setScopes("openid", "profile", "offline_access")
            .build()

        val authService = AuthorizationService(context)
        val intent = authService.getAuthorizationRequestIntent(authRequest)
        activity.startActivity(intent)
    }
}
