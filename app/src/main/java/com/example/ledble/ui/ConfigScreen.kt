package com.example.ledble.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ledble.data.ble.BleUUIDs
import com.example.ledble.viewmodels.BleViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(viewModel: BleViewModel, onDisconnect: () -> Unit) {
    val status by viewModel.status.collectAsState()
    var showSectionedView by remember { mutableStateOf(true) }

    // All known params (from your C++ code)
    val configs = remember {
        mutableStateMapOf(
            "displayBright" to "",
            "autoBrightMin" to "",
            "autoBrightMax" to "",
            "swapBlueGreen" to "",
            "swapBlueRed" to "",
            "use24hFormat" to "",
            "ldrPin" to "",
            "timeZone" to "",
            "wifiSsid" to "",
            "wifiPwd" to "",
            "ntpServer" to "",
            "canvasFile" to "",
            "canvasServer" to "",
            "manualPosix" to "",
            "displayRotation" to "",
            "driver" to "",
            "i2cSpeed" to "",
            "E_pin" to "",
            "firmwareVersion" to "",
            "firmwareName" to "",
            "clockfaceName" to ""
        )
    }

    // UUID -> param name
    val uuidToParam = remember {
        mapOf(
            BleUUIDs.DISPLAY_BRIGHT to "displayBright",
            BleUUIDs.AUTO_BRIGHT_MIN to "autoBrightMin",
            BleUUIDs.AUTO_BRIGHT_MAX to "autoBrightMax",
            BleUUIDs.SWAP_BLUE_GREEN to "swapBlueGreen",
            BleUUIDs.SWAP_BLUE_RED to "swapBlueRed",
            BleUUIDs.USE_24H_FORMAT to "use24hFormat",
            BleUUIDs.LDR_PIN to "ldrPin",
            BleUUIDs.TIME_ZONE to "timeZone",
            BleUUIDs.WIFI_SSID to "wifiSsid",
            BleUUIDs.WIFI_PWD to "wifiPwd",
            BleUUIDs.NTP_SERVER to "ntpServer",
            BleUUIDs.CANVAS_FILE to "canvasFile",
            BleUUIDs.CANVAS_SERVER to "canvasServer",
            BleUUIDs.MANUAL_POSIX to "manualPosix",
            BleUUIDs.DISPLAY_ROTATION to "displayRotation",
            BleUUIDs.DRIVER to "driver",
            BleUUIDs.I2C_SPEED to "i2cSpeed",
            BleUUIDs.E_PIN to "E_pin",
            BleUUIDs.FIRMWARE_VERSION to "firmwareVersion",
            BleUUIDs.FIRMWARE_NAME to "firmwareName",
            BleUUIDs.CLOCKFACE_NAME to "clockfaceName"
        )
    }

    // first load + listen for updates
    LaunchedEffect(Unit) {
        viewModel.readAllSettings()
        viewModel.status.collect { msg ->
            if (msg.startsWith("Read ")) {
                val parts = msg.split(": ", limit = 2)
                if (parts.size == 2) {
                    val uuidString = parts[0].removePrefix("Read ").trim()
                    val value = parts[1].trim()
                    try {
                        val uuid = UUID.fromString(uuidString)
                        val key = uuidToParam[uuid]
                        if (key != null) {
                            configs[key] = value
                        }
                    } catch (_: Exception) {
                        // ignore malformed
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp), // matches app bar height
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Configuration",
                            fontSize = 18.sp,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onDisconnect) {
                        Text("Back", style = MaterialTheme.typography.labelLarge)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            if (showSectionedView) "Sectioned" else "Flat",
                            style = MaterialTheme.typography.labelSmall
                        )
                        Switch(
                            checked = showSectionedView,
                            onCheckedChange = { showSectionedView = it },
                            modifier = Modifier.scale(0.8f) // smaller switch
                        )
                    }
                },
                modifier = Modifier.height(72.dp),
                colors = TopAppBarDefaults.topAppBarColors()
            )

        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(16.dp)) // extra space below top bar

            Text("Status: $status", style = MaterialTheme.typography.bodySmall)
            Divider(Modifier.padding(vertical = 12.dp))

            if (showSectionedView)
                SectionedView(configs, viewModel)
            else
                FlatView(configs, viewModel)
        }
    }
}

@Composable
private fun SectionedView(
    configs: Map<String, String>,
    viewModel: BleViewModel
) {
    Section("Display Settings") {
        ConfigInput(
            label = "Brightness",
            value = configs["displayBright"] ?: "",
            onSet = { viewModel.writeBrightness(it.toIntOrNull() ?: 0) },
            onRefresh = { viewModel.readBrightness() }
        )
    }

    Section("Network Settings") {
        ConfigInput(
            label = "Wi-Fi SSID",
            value = configs["wifiSsid"] ?: "",
            onSet = { viewModel.setWifi(it, configs["wifiPwd"] ?: "") },
            onRefresh = { viewModel.readAllSettings() } // or a dedicated read if you add it
        )
        ConfigInput(
            label = "Wi-Fi Password",
            value = configs["wifiPwd"] ?: "",
            isPassword = true,
            onSet = { viewModel.setWifi(configs["wifiSsid"] ?: "", it) },
            onRefresh = { viewModel.readAllSettings() }
        )
        ConfigInput(
            label = "Time Zone",
            value = configs["timeZone"] ?: "",
            placeholder = "America/Toronto",
            onSet = { viewModel.writeTimeZone(it) },
            onRefresh = { viewModel.readTimeZone() }
        )
    }

    Section("Canvas / Display Config") {
        ConfigInput(
            label = "Canvas Server",
            value = configs["canvasServer"] ?: "",
            placeholder = "http://canvas.local",
            onSet = { viewModel.writeCanvasServer(it) },
            onRefresh = { viewModel.readAllSettings() }
        )
    }

    Section("System Controls") {
        Button(
            onClick = { viewModel.restartDevice() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Restart Device") }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { viewModel.readAllSettings() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Reload All") }
    }
}

@Composable
private fun FlatView(
    configs: Map<String, String>,
    viewModel: BleViewModel
) {
    Text("Developer View – All Parameters", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))

    configs.forEach { (key, value) ->
        ConfigInput(
            label = key,
            value = value,
            onSet = { newValue ->
                when (key) {
                    "displayBright" -> viewModel.writeBrightness(newValue.toIntOrNull() ?: 0)
                    "wifiSsid" -> viewModel.setWifi(newValue, configs["wifiPwd"] ?: "")
                    "wifiPwd" -> viewModel.setWifi(configs["wifiSsid"] ?: "", newValue)
                    "timeZone" -> viewModel.writeTimeZone(newValue)
                    "canvasServer" -> viewModel.writeCanvasServer(newValue)
                    else -> {}
                }
            },
            onRefresh = { viewModel.readAllSettings() }
        )
    }

    Spacer(Modifier.height(20.dp))
    Button(onClick = { viewModel.readAllSettings() }, modifier = Modifier.fillMaxWidth()) {
        Text("Reload All")
    }
}

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(6.dp))
        content()
        Spacer(Modifier.height(12.dp))
        Divider()
    }
}

@Composable
private fun ConfigInput(
    label: String,
    value: String,
    placeholder: String = "",
    isPassword: Boolean = false,
    onSet: (String) -> Unit,
    onRefresh: () -> Unit
) {
    var text by remember(value) { mutableStateOf(value) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        placeholder = { if (placeholder.isNotEmpty()) Text(placeholder) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    )

    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onSet(text) },
            enabled = text.isNotBlank(),
            modifier = Modifier.weight(1f)
        ) {
            Text("Set")
        }
        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.weight(1f)
        ) {
            Text("Refresh")
        }
    }
    Spacer(Modifier.height(12.dp))
}
