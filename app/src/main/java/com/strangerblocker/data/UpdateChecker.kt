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
    /** True if this is a preview/pre-release version. */
    val isPreview: Boolean get() = latestVersion.contains("-p")

    /** True if this version is newer than [current] (semver compare, handles -pNN preview suffix). */
    fun isNewerThan(current: String): Boolean {
        val l = latestVersion.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
        val c = current.split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(l.size, c.size)) {
            val lv = l.getOrElse(i) { 0 }
            val cv = c.getOrElse(i) { 0 }
            if (lv != cv) return lv > cv
        }
        // Same base version — compare preview suffix. Higher = newer.
        val lp = latestVersion.split("-").getOrElse(1) { "" }.removePrefix("p").toIntOrNull() ?: Int.MAX_VALUE
        val cp = current.split("-").getOrElse(1) { "" }.removePrefix("p").toIntOrNull() ?: Int.MAX_VALUE
        return lp > cp
    }
}

/** Result of an update check — holds the newest stable and newest preview release. */
data class UpdateCheckResult(
    val stable: UpdateInfo?,
    val preview: UpdateInfo?,
) {
    val hasAny: Boolean get() = stable != null || preview != null
}

object UpdateChecker {

    private const val API_LIST =
        "https://api.github.com/repos/khrlagst/stranger-call-blocker/releases"

    /**
     * Fetch the newest applicable releases.
     *
     * Rules:
     * - [UpdateCheckResult.stable] is always the latest non-prerelease newer
     *   than [currentVersion].
     * - [UpdateCheckResult.preview] is the latest pre-release newer than
     *   [currentVersion], only included when [includePreview] is true OR the
     *   installed version is itself a preview.
     */
    fun check(currentVersion: String = "", includePreview: Boolean = false): UpdateCheckResult {
        val wantPreview = includePreview || currentVersion.contains("-p")
        return try {
            val response = fetch(API_LIST)
            val array = JSONArray(response)
            var stable: UpdateInfo? = null
            var preview: UpdateInfo? = null
            for (i in 0 until array.length()) {
                val release = array.getJSONObject(i)
                val info = parseRelease(release) ?: continue
                if (!info.isNewerThan(currentVersion) || info.latestVersion == currentVersion) continue
                val isPrerelease = release.optBoolean("prerelease", false)
                if (isPrerelease) {
                    if (wantPreview && preview == null) preview = info
                } else {
                    if (stable == null) stable = info
                }
                if (stable != null && (preview != null || !wantPreview)) break
            }
            UpdateCheckResult(stable, preview)
        } catch (_: Exception) {
            UpdateCheckResult(null, null)
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
            conn.setRequestProperty("User-Agent", "StrangerBlocker")
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
