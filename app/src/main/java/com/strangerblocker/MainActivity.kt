package com.strangerblocker

import android.Manifest
import android.app.role.RoleManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.strangerblocker.ui.MainScreen
import com.strangerblocker.ui.MainViewModel
import com.strangerblocker.ui.theme.StrangerBlockerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val roleRequestLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { onResume() }

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // Permission state is visible in the UI banner; no further action needed.
        // If denied, the service gracefully falls back to blocking all unknown
        // numbers (safe default).
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        // SMS blocking silently no-ops if denied (SmsReceiver never fires).
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (!isRoleHeld()) {
            requestCallScreeningRole()
        }
        requestContactsPermission()
        requestSmsPermission()

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            StrangerBlockerTheme(themeMode = themeMode) {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshRoleStatus()
        viewModel.refreshBlockingState()
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

    private fun requestContactsPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    private fun requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.RECEIVE_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionLauncher.launch(Manifest.permission.RECEIVE_SMS)
        }
    }
}
