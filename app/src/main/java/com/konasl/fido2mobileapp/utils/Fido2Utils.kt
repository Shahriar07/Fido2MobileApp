package com.konasl.fido2mobileapp.utils

import com.google.android.gms.fido.fido2.api.common.AuthenticatorErrorResponse

/**
 * Utility class for FIDO2 operations
 */
object Fido2Utils {

    /**
     * Helper function to get a user-friendly error message from FIDO2 error codes
     * @param errorResponse The AuthenticatorErrorResponse from FIDO2 API
     * @return A human-readable error message
     */
    fun getFido2ErrorMessage(errorResponse: AuthenticatorErrorResponse): String {
        return when (errorResponse.errorCode.code) {
            // Common FIDO2 error codes
            1 -> "The operation was canceled by the user"
            2 -> "The operation timed out"
            3 -> "The operation is not supported"
            4 -> "Client data validation failed"
            5 -> "Operation is waiting for user verification"
            6 -> "Key handle not recognized"
            7 -> "Algorithm not supported"
            8 -> "Not allowed operation"
            9 -> "No suitable authenticator found"
            10 -> "Invalid request data"
            11 -> "Missing parameter in the request"
            12 -> "Not allowed to use this authenticator"
            14 -> "User verification requirement cannot be met"
            15 -> "Operation denied by the user"
            16 -> "Key storage is full"
            17 -> "No operation is pending"
            18 -> "The incoming request cannot be validated" // This usually happens when:
                  // 1. Challenge mismatch between request and what authenticator expected
                  // 2. Origin validation failure (request is from different origin than expected)
                  // 3. Malformed request parameters
                  // 4. Security key/authenticator doesn't recognize the relying party
                  // 5. Timing issues with the request (expired challenge)
            else -> errorResponse.errorMessage ?: "Unknown error occurred"
        }
    }
}
