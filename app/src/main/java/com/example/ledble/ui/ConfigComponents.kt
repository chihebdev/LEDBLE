package com.example.ledble.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ConfigTextField(label: String, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { onSubmit(text) }, modifier = Modifier.padding(top = 4.dp)) {
        Text("Set")
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun ConfigPasswordField(label: String, onSubmit: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth()
    )
    Button(onClick = { onSubmit(text) }, modifier = Modifier.padding(top = 4.dp)) {
        Text("Set")
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun ConfigSwitch(label: String, onToggle: (Boolean) -> Unit) {
    var checked by remember { mutableStateOf(false) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label)
        Switch(checked = checked, onCheckedChange = {
            checked = it
            onToggle(it)
        })
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun ConfigSlider(label: String, range: IntRange, onValueChange: (Int) -> Unit) {
    var value by remember { mutableStateOf(range.first.toFloat()) }
    Text("$label: ${value.toInt()}")
    Slider(
        value = value,
        onValueChange = { value = it },
        onValueChangeFinished = { onValueChange(value.toInt()) },
        valueRange = range.first.toFloat()..range.last.toFloat()
    )
    Spacer(Modifier.height(8.dp))
}
