package com.gruop11.yepapp.platform.sync

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object YepSyncClient {
    const val SYNC_BASE_URL = "https://ais-pre-4m6nrrehsj2iox7sbt5epx-74657253484.europe-west2.run.app"

    data class PullResult(
        val unchanged: Boolean,
        val version: Int,
        val lastClientId: String,
        val state: JSONObject?
    )

    suspend fun pullState(sinceVersion: Int = 0): PullResult? = withContext(Dispatchers.IO) {
        val query = if (sinceVersion > 0) "?sinceVersion=$sinceVersion" else ""
        val conn = (URL("$SYNC_BASE_URL/api/sync/state$query").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Cache-Control", "no-store")
        }
        try {
            if (conn.responseCode !in 200..299) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            PullResult(
                unchanged = json.optBoolean("unchanged", false),
                version = json.optInt("version", 0),
                lastClientId = json.optString("lastClientId", ""),
                state = json.optJSONObject("state")
            )
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    suspend fun pushState(clientId: String, state: JSONObject): Int? = withContext(Dispatchers.IO) {
        val conn = (URL("$SYNC_BASE_URL/api/sync/state").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 10000
            readTimeout = 10000
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val payload = JSONObject().apply {
                put("clientId", clientId)
                put("state", state)
            }
            conn.outputStream.bufferedWriter().use { it.write(payload.toString()) }
            if (conn.responseCode !in 200..299) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            if (json.optBoolean("ok", false)) json.optInt("version", 0) else null
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    suspend fun resetState(clientId: String): PullResult? = withContext(Dispatchers.IO) {
        val conn = (URL("$SYNC_BASE_URL/api/sync/reset").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            conn.outputStream.bufferedWriter().use {
                it.write(JSONObject().put("clientId", clientId).toString())
            }
            if (conn.responseCode !in 200..299) return@withContext null
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            PullResult(
                unchanged = false,
                version = json.optInt("version", 0),
                lastClientId = clientId,
                state = json.optJSONObject("state")
            )
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Converts Android Gallery/PhotoPicker content:// URIs into a compact 240x240 JPEG base64 Data URL
     * so profile avatars edited on Android display on the Web App (and vice versa).
     */
    fun encodeAvatarToDataUrl(context: Context, uriOrDataUrl: String?): String? {
        if (uriOrDataUrl.isNullOrBlank()) return null
        if (uriOrDataUrl.startsWith("data:") || uriOrDataUrl.startsWith("http")) return uriOrDataUrl
        return try {
            val uri = Uri.parse(uriOrDataUrl)
            val original = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: return null
            val maxDim = 240f
            val scale = minOf(maxDim / original.width, maxDim / original.height, 1f)
            val scaled = if (scale < 1f) {
                Bitmap.createScaledBitmap(
                    original,
                    (original.width * scale).toInt().coerceAtLeast(1),
                    (original.height * scale).toInt().coerceAtLeast(1),
                    true
                )
            } else original
            val out = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
            "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } catch (_: Exception) {
            null
        }
    }
}
