package com.konasl.fido2mobileapp.utils

object Constants {
    const val CLIENT_ID = "0oaowg1kucupMxmmY5d7"
    const val REDIRECT_URI = "com.konasl.fido2mobileapp:/callback"
    const val AUTHORIZATION_ENDPOINT = "https://dev-12202026.okta.com/oauth2/v1/authorize"
    const val RESPONSE_TYPE = "code"
    const val SCOPE = "openid"
    const val STATE = "abc124"
    const val NONCE = "xyz789"
    const val IDP_ID = "0oaowiju4yrqv3ldC5d7"
//    const val BASE_URL = "http://10.88.231.251:9080"
    const val BASE_URL = "https://dev-kidp.konancp.com"
    const val FIDO2_REGISTER_PATH = "/api/webauthn/register/options"
    const val FIDO2_REGISTER_COMPLETE_PATH = "/api/webauthn/register/complete"

    const val FIDO2_AUTHENTICATION_PATH = "/api/webauthn/authenticate/options"
    const val FIDO2_AUTHENTICATION_COMPLETE_PATH = "/api/webauthn/authenticate/complete"
}
