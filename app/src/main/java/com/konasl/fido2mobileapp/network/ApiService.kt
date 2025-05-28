package com.konasl.fido2mobileapp.network

import okhttp3.*
import org.json.JSONObject
import java.io.IOException

object ApiService {
    private val client = OkHttpClient()

    fun postForm(
        url: String,
        formParams: Map<String, String>,
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val formBodyBuilder = FormBody.Builder()
        formParams.forEach { (k, v) -> formBodyBuilder.add(k, v) }

        val request = Request.Builder()
            .url(url)
            .post(formBodyBuilder.build())
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    callback(true, it)
                } ?: callback(false, null)
            }
        })
    }

    fun get(
        url: String,
        headers: Map<String, String> = emptyMap(),
        callback: (success: Boolean, response: String?) -> Unit
    ) {
        val requestBuilder = Request.Builder().url(url)
        headers.forEach { (k, v) -> requestBuilder.header(k, v) }

        client.newCall(requestBuilder.build()).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                callback(true, response.body?.string())
            }
        })
    }
}
