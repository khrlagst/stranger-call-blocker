package com.strangerblocker.data

import org.json.JSONArray
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

    private const val API_LATEST =
        "https://api.github.com/repos/khrlagst/stranger-call-blocker/releases/latest"
    private const val API_LIST =
        "https://api.github.com/repos/khrlagst/stranger-call-blocker/releases"

    /**
     * Fetch the latest applicable release. If [currentVersion] contains `-p`
     * (preview), searches all releases including pre-releases so preview
     * builds can update to newer previews. Otherwise fetches only the latest
     * stable release.
     */
    fun check(currentVersion: String = ""): UpdateInfo? {
        val isPreview = currentVersion.contains("-p")
        return if (isPreview) checkFromList(currentVersion) else checkLatest()
    }

    private fun checkLatest(): UpdateInfo? {
        return try {
            val response = fetch(API_LATEST)
            val json = JSONObject(response)
            parseRelease(json)
        } catch (_: Exception) {
            null
        }
    }

    private fun checkFromList(currentVersion: String): UpdateInfo? {
        return try {
            val response = fetch(API_LIST)
            val array = JSONArray(response)
            for (i in 0 until array.length()) {
                val release = array.getJSONObject(i)
                val info = parseRelease(release) ?: continue
                if (info.isNewerThan(currentVersion) && info.latestVersion != currentVersion) {
                    return info
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun parseRelease(json: JSONObject): UpdateInfo? {
        val tag = json.optString("tag_name", "") ?: return null
        if (tag.isEmpty()) return null
        val version = tag.removePrefix("v")
        val assets = json.optJSONArray("assets")
        val url = if (assets != null && assets.length() > 0) {
            assets.getJSONObject(0).optString("browser_download_url", "")
        } else ""
        if (url.isEmpty()) return null
        return UpdateInfo(
            latestVersion = version,
            downloadUrl = url,
            releaseNotes = json.optString("body", ""),
        )
    }

    private fun fetch(url: String): String {
        return URL(url).openConnection().let { conn ->
            conn as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.inputStream.bufferedReader().readText()
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
