package com.rimaro.musify.utils

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import kotlin.collections.iterator

class MyDownloader : Downloader() {
    private val client = OkHttpClient()

    override fun execute(request: Request): Response {
        val body1 = request.dataToSend()?.toRequestBody()

        val req = okhttp3.Request.Builder()
            .url(request.url())
            .method(request.httpMethod(), body1)
            .apply {
                for ((key, values) in request.headers()) {
                    for(value in values){
                        addHeader(key, value)
                    }
                }
                addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; rv:78.0) Gecko/20100101 Firefox/78.0")
            }
            .build()

        val response = client.newCall(req).execute()
        val body = response.body?.string() ?: ""

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            body,
            response.request.url.toString()
        )
    }

    private fun getRequestBody(method: String): RequestBody? {
        Log.d("MyDownloader", "Method: $method")
        return when (method.uppercase()) {
            "POST", "PUT", "PATCH" -> ByteArray(0).toRequestBody() // empty body
            else -> null // GET, HEAD, DELETE usually don't need a body
        }
    }
}