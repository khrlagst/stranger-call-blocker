package com.strangerblocker.service

import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.strangerblocker.R

@RequiresApi(Build.VERSION_CODES.N)
class BlockerTileService : TileService() {

    private val prefs: SharedPreferences by lazy {
        getSharedPreferences("stranger_blocker", MODE_PRIVATE)
    }

    private companion object {
        const val PAUSE_MS = 60 * 60 * 1000L
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        if (isPaused()) {
            prefs.edit().putLong("blocking_paused_until", 0L).commit()
            updateTile()
            showToast("Screening is active again")
        } else {
            prefs.edit().putLong(
                "blocking_paused_until",
                System.currentTimeMillis() + PAUSE_MS,
            ).commit()
            updateTile()
            showToast("Screening is paused for 1 hour")
        }
    }

    private fun isPaused(): Boolean =
        prefs.getLong("blocking_paused_until", 0L) > System.currentTimeMillis()

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateTile() {
        val active = !isPaused() && prefs.getBoolean("blocking_enabled", true)
        qsTile?.let { tile ->
            tile.state = if (active) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            tile.label = if (active) "Blocking" else "Paused"
            tile.subtitle = if (active) "Stranger Blocker" else "Tap to resume"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_shield)
            tile.updateTile()
        }
    }
}
