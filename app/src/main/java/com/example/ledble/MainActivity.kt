package com.example.ledble

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import com.example.ledble.ui.BleScreen
import com.example.ledble.ui.ConfigScreen
import com.example.ledble.ui.theme.LEDBLETheme
import com.example.ledble.viewmodels.BleViewModel

class MainActivity : ComponentActivity() {

    private val permissions = buildList {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }.toTypedArray()

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val denied = result.entries.filter { !it.value }.map { it.key }
            if (denied.isNotEmpty()) {
                println("Denied permissions: $denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasAllPermissions()) {
            permissionLauncher.launch(permissions)
        }

        val viewModel = BleViewModel(this)

        setContent {
            LEDBLETheme {
                var isConnected by remember { mutableStateOf(false) }

                if (!isConnected) {
                    BleScreen(
                        viewModel = viewModel,
                        activity = this,
                        onConnected = { isConnected = true }
                    )
                } else {
                    ConfigScreen(viewModel = viewModel, onDisconnect = { isConnected = false })
                }
            }
        }
    }

    private fun hasAllPermissions(): Boolean {
        return permissions.all {
            ActivityCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
