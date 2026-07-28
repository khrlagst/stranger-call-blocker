package com.strangerblocker.ui

import android.app.Application
import android.app.role.RoleManager
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.strangerblocker.StrangerBlockerApp
import com.strangerblocker.data.BlockedCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("stranger_blocker", Context.MODE_PRIVATE)

    private val db = (application as StrangerBlockerApp).db

    /** Blocked-call history, observed live from Room. */
    val blockedCalls: Flow<List<BlockedCall>> = db.blockedCallDao().observeAll()

    /** Toggle state. */
    val isBlockingEnabled: StateFlow<Boolean> = MutableStateFlow(
        prefs.getBoolean("blocking_enabled", true)
    ).asStateFlow()

    /** Role grant state. */
    val isRoleHeld: StateFlow<Boolean> = MutableStateFlow(
        checkRoleHeld()
    ).asStateFlow()

    private fun checkRoleHeld(): Boolean {
        val roleManager = getApplication<Application>()
            .getSystemService(Context.ROLE_SERVICE) as RoleManager
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    fun toggleBlocking(enabled: Boolean) {
        prefs.edit().putBoolean("blocking_enabled", enabled).apply()
        (isBlockingEnabled as MutableStateFlow).value = enabled
    }

    fun refreshRoleStatus() {
        (isRoleHeld as MutableStateFlow).value = checkRoleHeld()
    }

    fun clearHistory() {
        viewModelScope.launch {
            db.blockedCallDao().clearAll()
        }
    }
}
