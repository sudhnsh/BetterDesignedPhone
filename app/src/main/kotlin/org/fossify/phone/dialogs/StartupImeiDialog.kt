package org.fossify.phone.dialogs

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.phone.extensions.config

@Composable
fun StartupImeiDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val config = remember { context.config }
    val detected = remember { detectHardwareImeis(context) }
    var imei1 by remember { mutableStateOf(detected.getOrNull(0)?.second ?: config.customImei1) }
    var imei2 by remember { mutableStateOf(detected.getOrNull(1)?.second ?: config.customImei2) }

    AlertDialog(
        onDismissRequest = {
            config.hasPromptedImeiSetup = true
            onDismiss()
        },
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Welcome to APhone",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Configure your device IMEI for quick *#06# barcode scanning.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Android OS often restricts dialers from reading hardware IMEI directly. Enter your IMEI below so it's instantly available when you dial *#06#. You can update this anytime in Settings.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                OutlinedTextField(
                    value = imei1,
                    onValueChange = { input ->
                        imei1 = input.filter { it.isDigit() }.take(16)
                    },
                    label = { Text("IMEI 1 (15 digits)") },
                    placeholder = { Text("Enter IMEI 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    trailingIcon = {
                        if (imei1.isNotEmpty()) {
                            IconButton(onClick = { imei1 = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = imei2,
                    onValueChange = { input ->
                        imei2 = input.filter { it.isDigit() }.take(16)
                    },
                    label = { Text("IMEI 2 (Optional)") },
                    placeholder = { Text("Enter IMEI 2 (for dual SIM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    trailingIcon = {
                        if (imei2.isNotEmpty()) {
                            IconButton(onClick = { imei2 = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    config.customImei1 = imei1.trim()
                    config.customImei2 = imei2.trim()
                    config.hasPromptedImeiSetup = true
                    Toast.makeText(context, "IMEI saved! You can dial *#06# anytime.", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    config.hasPromptedImeiSetup = true
                    onDismiss()
                }
            ) {
                Text("Skip")
            }
        }
    )
}
