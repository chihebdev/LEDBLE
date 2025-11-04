package com.example.ledble.data.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

class BleRepository(private val context: Context) {

    private val _messages = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val messages = _messages.asSharedFlow()

    private var bluetoothGatt: BluetoothGatt? = null

    fun connect(device: BluetoothDevice) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            _messages.tryEmit("Missing BLUETOOTH_CONNECT permission")
            return
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt = device.connectGatt(context, false, gattCallback)
        _messages.tryEmit("Connecting to ${device.name ?: "Unknown"}")
    }

    fun disconnect() {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        _messages.tryEmit("Disconnected")
    }

    fun readCharacteristic(serviceUUID: UUID, characteristicUUID: UUID) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) return

        bluetoothGatt?.getService(serviceUUID)
            ?.getCharacteristic(characteristicUUID)
            ?.let {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                bluetoothGatt?.readCharacteristic(it)
                _messages.tryEmit("Reading $characteristicUUID")
            } ?: _messages.tryEmit("Characteristic not found: $characteristicUUID")
    }

    @SuppressLint("MissingPermission")
    fun writeCharacteristic(serviceUUID: UUID, characteristicUUID: UUID, value: ByteArray) {
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            _messages.tryEmit("Missing BLUETOOTH_CONNECT permission")
            return
        }

        val gatt = bluetoothGatt
        if (gatt == null) {
            _messages.tryEmit("No GATT connection available")
            return
        }

        val characteristic = gatt
            .getService(serviceUUID)
            ?.getCharacteristic(characteristicUUID)

        if (characteristic == null) {
            _messages.tryEmit("Characteristic not found: $characteristicUUID")
            return
        }

        // Determine correct write type based on characteristic properties
        characteristic.writeType =
            if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0)
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            else
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

        // Use correct write API depending on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val status = gatt.writeCharacteristic(characteristic, value, characteristic.writeType)
            if (status == BluetoothStatusCodes.SUCCESS) {
                _messages.tryEmit("Writing ${characteristic.uuid}: ${value.decodeToString()}")
            } else {
                _messages.tryEmit("Write failed (status=$status) to ${characteristic.uuid}")
            }
        } else {
            @Suppress("DEPRECATION")
            characteristic.setValue(value)
            @Suppress("DEPRECATION")
            val result = gatt.writeCharacteristic(characteristic)
            if (result) {
                _messages.tryEmit("Writing ${characteristic.uuid}: ${value.decodeToString()}")
            } else {
                _messages.tryEmit("Write failed to ${characteristic.uuid}")
            }
        }
    }


    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    _messages.tryEmit("Connected to ${gatt.device.name ?: "device"}")
                    if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        gatt.discoverServices()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    _messages.tryEmit("Disconnected")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _messages.tryEmit("Services discovered (${gatt.services.size})")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val value = characteristic.value?.decodeToString() ?: "(null)"
            _messages.tryEmit("Read ${characteristic.uuid}: $value")
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            _messages.tryEmit("Wrote ${characteristic.uuid}")
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }
}