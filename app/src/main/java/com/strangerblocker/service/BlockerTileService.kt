package com.strangerblocker.service

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
class BlockerTileService : TileService() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("stranger_blocker", MODE_PRIVATE)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        val current = prefs.getBoolean("blocking_enabled", true)
        prefs.edit().putBoolean("blocking_enabled", !current).apply()
        updateTile()
    }

    private fun updateTile() {
        val enabled = prefs.getBoolean("blocking_enabled", true)
        qsTile?.let { tile ->
            tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = if (enabled) "Blocking" else "Off"
            tile.subtitle = if (enabled) "Stranger Blocker" else "Tap to enable"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_shield)
            tile.updateTile()
        }
    }
}
