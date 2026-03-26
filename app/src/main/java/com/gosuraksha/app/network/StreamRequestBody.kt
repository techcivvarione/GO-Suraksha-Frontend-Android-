package com.gosuraksha.app.network

import android.content.Context
import android.net.Uri
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

class StreamRequestBody(
    private val context: Context,
    private val uri: Uri,
    private val mimeType: String
) : RequestBody() {

    override fun contentType(): MediaType? = mimeType.toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            sink.writeAll(inputStream.source())
        } ?: error("Unable to open input stream for uri: $uri")
    }
}
