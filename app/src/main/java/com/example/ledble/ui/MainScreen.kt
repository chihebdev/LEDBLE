package com.example.ledble.ui

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.ledble.MainActivity
import com.example.ledble.viewmodels.BleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleScreen(
    viewModel: BleViewModel,
    activity: MainActivity,
    onConnected: () -> Unit
) {
    val devices by viewModel.devices.collectAsState()
    val status by viewModel.status.collectAsState()

    var isScanning by remember { mutableStateOf(false) }
    var connectedDevice by remember { mutableStateOf<BluetoothDevice?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clockwise BLE Config") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Status: $status", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        isScanning = true
                        connectedDevice = null
                        viewModel.startScan()
                    },
                    enabled = !isScanning
                ) { Text("Start Scan") }

                Button(
                    onClick = {
                        isScanning = false
                        viewModel.stopScan()
                    },
                    enabled = isScanning
                ) { Text("Stop Scan") }
            }

            Spacer(Modifier.height(16.dp))

            Text("Available Devices", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(devices) { device ->
                    DeviceCard(
                        device = device,
                        isConnected = connectedDevice?.address == device.address,
                        onClick = {
                            // stop scan, connect, and go to config right away
                            isScanning = false
                            connectedDevice = device
                            viewModel.connectTo(device)
                            onConnected()
                        },
                        activity
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: BluetoothDevice,
    isConnected: Boolean,
    onClick: () -> Unit,
    activity: MainActivity
) {
    val containerColor =
        if (isConnected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.padding(12.dp)) {
            if (ActivityCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
            }
            Text(device.name ?: "Unnamed Device", style = MaterialTheme.typography.titleMedium)
            Text(device.address, style = MaterialTheme.typography.bodySmall)
            if (isConnected) Text("Connected", style = MaterialTheme.typography.labelMedium)
        }
    }
}
