package org.fossify.phone.dialogs

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.extensions.config

@Composable
fun ConfigureImeiDialog(
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val config = remember { context.config }
    var imei1 by remember { mutableStateOf(config.customImei1) }
    var imei2 by remember { mutableStateOf(config.customImei2) }

    val detectedImeis = remember { detectHardwareImeis(context) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                text = "Device IMEI Configuration",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Add or customize your IMEI numbers. When dialed (*#06#), the app will display these numbers and their scannable barcodes.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                OutlinedTextField(
                    value = imei1,
                    onValueChange = { input ->
                        imei1 = input.filter { it.isDigit() }.take(16)
                    },
                    label = { Text("IMEI 1 (15 digits)") },
                    placeholder = { Text("e.g. 864520040123456") },
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
                    placeholder = { Text("e.g. 864520040123457") },
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

                // Real Hardware IMEI Inspection Section
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.height(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Real Hardware IMEI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (detectedImeis.isNotEmpty()) {
                            detectedImeis.forEach { (label, value) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (imei1.isEmpty()) imei1 = value else if (imei2.isEmpty()) imei2 = value
                                        }
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = value, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                                    }
                                    Text(
                                        text = "Use this",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "Android OS blocks direct hardware reading for third-party dialers on modern versions.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    })
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Could not open system settings", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View Real IMEI in System Settings", fontSize = 12.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    config.customImei1 = imei1.trim()
                    config.customImei2 = imei2.trim()
                    Toast.makeText(context, "IMEI settings saved", Toast.LENGTH_SHORT).show()
                    onSaved()
                    onDismiss()
                }
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            Row {
                if (config.customImei1.isNotEmpty() || config.customImei2.isNotEmpty()) {
                    TextButton(
                        onClick = {
                            config.customImei1 = ""
                            config.customImei2 = ""
                            imei1 = ""
                            imei2 = ""
                            Toast.makeText(context, "Custom IMEI cleared", Toast.LENGTH_SHORT).show()
                            onSaved()
                            onDismiss()
                        }
                    ) {
                        Text("Clear")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

fun showConfigureImeiDialog(activity: SimpleActivity, onSaved: () -> Unit = {}) {
    var dialog: AlertDialog? = null
    val composeView = ComposeView(activity).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            PhoneTheme(activity = activity) {
                ConfigureImeiDialog(
                    onDismiss = { dialog?.dismiss() },
                    onSaved = onSaved
                )
            }
        }
    }

    dialog = AlertDialog.Builder(activity)
        .setView(composeView)
        .create()
        .apply {
            window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            show()
        }
}

fun detectHardwareImeis(context: Context): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val tm = context.getSystemService(TelephonyManager::class.java) ?: return list

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val imei0 = tm.getImei(0)
            if (!imei0.isNullOrBlank()) list.add("System IMEI 1" to imei0)
            val imei1 = tm.getImei(1)
            if (!imei1.isNullOrBlank()) list.add("System IMEI 2" to imei1)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val imei0 = tm.imei
            if (!imei0.isNullOrBlank()) list.add("System IMEI" to imei0)
        } else {
            @Suppress("DEPRECATION")
            val deviceId = tm.deviceId
            if (!deviceId.isNullOrBlank()) list.add("System IMEI" to deviceId)
        }
    } catch (_: Exception) {
    }
    return list
}
