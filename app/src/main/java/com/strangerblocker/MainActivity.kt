package com.strangerblocker

import android.app.role.RoleManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import com.strangerblocker.ui.MainScreen
import com.strangerblocker.ui.theme.StrangerBlockerTheme

class MainActivity : ComponentActivity() {

    /** Request launcher for ROLE_CALL_SCREENING grant. */
    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { /* result — user returned from role grant screen; UI refreshes on resume */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // If the role is not held, prompt the user on first launch.
        if (!isRoleHeld()) {
            requestCallScreeningRole()
        }

        setContent {
            StrangerBlockerTheme {
                MainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Role state may have changed while we were away — the ViewModel
        // refresh is handled internally via its own lifecycle awareness.
    }

    private fun isRoleHeld(): Boolean {
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        return roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)
    }

    private fun requestCallScreeningRole() {
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        val intent: Intent? = roleManager.createRequestRoleIntent(
            RoleManager.ROLE_CALL_SCREENING
        )
        if (intent != null) {
            roleRequestLauncher.launch(intent)
        }
    }
}
