// SPDX-License-Identifier: Apache-2.0
package com.strangerblocker.engine

import android.content.SharedPreferences

/** [SenderStore] backed by [SharedPreferences]. */
class PrefsSenderStore(private val prefs: SharedPreferences) : SenderStore {

    override fun getStringSet(key: String, default: Set<String>): Set<String> =
        prefs.getStringSet(key, default) ?: default

    override fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }
}
