package com.example.ledble.viewmodels

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ledble.data.ble.BleRepository
import com.example.ledble.data.ble.BleUUIDs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class BleViewModel(private val context: Context) : ViewModel() {

    private val adapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val scanner: BluetoothLeScanner? = adapter?.bluetoothLeScanner
    val repo = BleRepository(context)

    private val _devices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val devices = _devices.asStateFlow()

    private val _status = MutableStateFlow("Idle")
    val status = _status.asStateFlow()

    init {
        viewModelScope.launch {
            repo.messages.collect { msg -> _status.value = msg }
        }
    }

    // ------------------ BLE Scan & Connection ------------------

    @SuppressLint("MissingPermission")
    fun startScan(serviceUUID: UUID? = null) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            _status.value = "Missing BLUETOOTH_SCAN permission"
            return
        }

        val filters = if (serviceUUID != null) {
            listOf(ScanFilter.Builder().setServiceUuid(ParcelUuid(serviceUUID)).build())
        } else emptyList()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        _devices.value = emptyList()
        scanner?.startScan(filters, settings, callback)
        _status.value = "Scanning..."
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        scanner?.stopScan(callback)
        _status.value = "Scan stopped"
    }

    @SuppressLint("MissingPermission")
    fun connectTo(device: BluetoothDevice) {
        stopScan()
        _status.value = "Connecting to ${device.name ?: "Unknown"}"
        repo.connect(device)
    }

    private val callback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            if (device.name != null && !_devices.value.any { it.address == device.address }) {
                _devices.value = _devices.value + device
            }
        }

        override fun onScanFailed(errorCode: Int) {
            _status.value = "Scan failed: $errorCode"
        }
    }

    // ------------------ Configuration Reads ------------------

    fun readAllSettings() {
        val servicePairs = listOf(
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.DISPLAY_BRIGHT,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.AUTO_BRIGHT_MIN,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.AUTO_BRIGHT_MAX,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.SWAP_BLUE_GREEN,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.SWAP_BLUE_RED,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.USE_24H_FORMAT,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.LDR_PIN,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.DISPLAY_ROTATION,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.DRIVER,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.I2C_SPEED,
            BleUUIDs.DEVICE_CONFIG_SERVICE to BleUUIDs.E_PIN,

            BleUUIDs.NETWORK_SERVICE to BleUUIDs.WIFI_SSID,
            BleUUIDs.NETWORK_SERVICE to BleUUIDs.WIFI_PWD,
            BleUUIDs.NETWORK_SERVICE to BleUUIDs.TIME_ZONE,
            BleUUIDs.NETWORK_SERVICE to BleUUIDs.NTP_SERVER,
            BleUUIDs.NETWORK_SERVICE to BleUUIDs.MANUAL_POSIX,

            BleUUIDs.DISPLAY_SERVICE to BleUUIDs.CANVAS_FILE,
            BleUUIDs.DISPLAY_SERVICE to BleUUIDs.CANVAS_SERVER,

            BleUUIDs.DEVICE_INFO_SERVICE to BleUUIDs.FIRMWARE_VERSION,
            BleUUIDs.DEVICE_INFO_SERVICE to BleUUIDs.FIRMWARE_NAME,
            BleUUIDs.DEVICE_INFO_SERVICE to BleUUIDs.CLOCKFACE_NAME
        )

        for ((service, char) in servicePairs) {
            repo.readCharacteristic(service, char)
        }
    }

    fun readBrightness() {
        repo.readCharacteristic(BleUUIDs.DEVICE_CONFIG_SERVICE, BleUUIDs.DISPLAY_BRIGHT)
    }

    fun readTimeZone() {
        repo.readCharacteristic(BleUUIDs.NETWORK_SERVICE, BleUUIDs.TIME_ZONE)
    }

    // ------------------ Configuration Writes ------------------

    fun writeBrightness(value: Int) {
        repo.writeCharacteristic(
            BleUUIDs.DEVICE_CONFIG_SERVICE,
            BleUUIDs.DISPLAY_BRIGHT,
            byteArrayOf(value.toByte())
        )
    }

    fun writeTimeZone(tz: String) {
        repo.writeCharacteristic(
            BleUUIDs.NETWORK_SERVICE,
            BleUUIDs.TIME_ZONE,
            tz.toByteArray()
        )
    }

    fun writeCanvasServer(url: String) {
        repo.writeCharacteristic(
            BleUUIDs.DISPLAY_SERVICE,
            BleUUIDs.CANVAS_SERVER,
            url.toByteArray()
        )
    }

    fun setWifi(ssid: String, password: String) {
        repo.writeCharacteristic(
            BleUUIDs.NETWORK_SERVICE,
            BleUUIDs.WIFI_SSID,
            ssid.toByteArray()
        )
        repo.writeCharacteristic(
            BleUUIDs.NETWORK_SERVICE,
            BleUUIDs.WIFI_PWD,
            password.toByteArray()
        )
    }

    fun restartDevice() {
        repo.writeCharacteristic(
            BleUUIDs.DEVICE_INFO_SERVICE,
            BleUUIDs.RESTART,
            byteArrayOf(1)
        )
    }

    // ------------------ Utility ------------------

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) ==
                PackageManager.PERMISSION_GRANTED
    }
}
