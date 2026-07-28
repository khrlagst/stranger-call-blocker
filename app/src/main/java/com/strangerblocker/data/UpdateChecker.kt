package com.strangerblocker.data

import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val latestVersion: String,
    val downloadUrl: String,
    val releaseNotes: String,
) {
    /** True if this version is newer than [current] (semver compare). */
    fun isNewerThan(current: String): Boolean {
        val l = latestVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        return false
    }
}

object UpdateChecker {

    private const val API_URL =
        "https://api.github.com/repos/khrlagst/stranger-call-blocker/releases/latest"

    /**
     * Fetch the latest release from GitHub. Returns null on failure
     * (network error, rate limit, or no release yet).
     */
    fun check(): UpdateInfo? {
        return try {
            val response = URL(API_URL).openConnection().let { conn ->
                conn as HttpURLConnection
                conn.connectTimeout = 10_000
                conn.readTimeout = 10_000
                conn.inputStream.bufferedReader().readText()
            }
            val json = JSONObject(response)
            val tag = json.getString("tag_name")          // "v1.1.0"
            val notes = json.optString("body", "")
            val assets = json.getJSONArray("assets")
            val url = if (assets.length() > 0) {
                assets.getJSONObject(0).getString("browser_download_url")
            } else return null

            UpdateInfo(
                latestVersion = tag.removePrefix("v"),
                downloadUrl = url,
                releaseNotes = notes,
            )
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Download APK from [url] to [dest]. Blocks the calling thread.
     */
    fun download(url: String, dest: File) {
        URL(url).openStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
