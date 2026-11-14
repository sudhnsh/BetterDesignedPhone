package org.fossify.phone.compose.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.provider.Telephony.Sms.Intents.SECRET_CODE_ACTION
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.HapticFeedbackConstants
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.fossify.commons.extensions.isDefaultDialer
import org.fossify.commons.helpers.KeypadHelper
import org.fossify.commons.helpers.isOreoPlus
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.compose.components.ContactAvatar
import org.fossify.phone.compose.components.HighlightedText
import org.fossify.phone.extensions.config
import org.fossify.phone.helpers.DIALPAD_TONE_LENGTH_MS
import org.fossify.phone.helpers.RecentsHelper
import org.fossify.phone.helpers.ToneGeneratorHelper
import org.fossify.phone.models.SpeedDial

data class KeypadButton(
    val char: Char,
    val letters: String
)

@Composable
fun DialpadScreen(
    dialedNumber: String,
    onAppendChar: (Char) -> Unit,
    onDeleteChar: () -> Unit,
    onClearAll: () -> Unit,
    onSetNumber: (String) -> Unit,
    allContacts: List<Contact>,
    speedDialValues: List<SpeedDial>,
    onCallClick: (String, String?) -> Unit,
    onCallWithSimSelector: (String) -> Unit,
    onContactDetailsClick: (Contact) -> Unit,
    onAddContactClick: (String) -> Unit = {},
    showImeiDialog: Boolean = false,
    onDismissImeiDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val view = LocalView.current
    val clipboardManager = LocalClipboardManager.current
    val activity = context as? SimpleActivity
    val coroutineScope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current

    val toneGenerator = remember { ToneGeneratorHelper(context, DIALPAD_TONE_LENGTH_MS) }

    DisposableEffect(Unit) {
        onDispose {
            toneGenerator.stopTone()
        }
    }

    if (showImeiDialog) {
        val deviceInfo = remember { getDeviceInfo(context) }
        ImeiDialog(
            deviceInfo = deviceInfo,
            onDismiss = onDismissImeiDialog
        )
    }

    // Filter contacts based on T9 or phone number matching
    val matchingContacts = remember(allContacts, dialedNumber) {
        if (dialedNumber.isEmpty()) {
            emptyList()
        } else {
            allContacts.filter { contact ->
                val convertedName = KeypadHelper.convertKeypadLettersToDigits(contact.getNameToDisplay())
                contact.doesContainPhoneNumber(dialedNumber) || convertedName.contains(dialedNumber, true)
            }.sortedBy { !it.doesContainPhoneNumber(dialedNumber) }
        }
    }

    val keys = remember {
        listOf(
            listOf(KeypadButton('1', ""), KeypadButton('2', "ABC"), KeypadButton('3', "DEF")),
            listOf(KeypadButton('4', "GHI"), KeypadButton('5', "JKL"), KeypadButton('6', "MNO")),
            listOf(KeypadButton('7', "PQRS"), KeypadButton('8', "TUV"), KeypadButton('9', "WXYZ")),
            listOf(KeypadButton('*', ""), KeypadButton('0', "+"), KeypadButton('#', ""))
        )
    }

    fun playToneAndHaptic(char: Char) {
        if (activity?.config?.dialpadVibration == true) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
        if (activity?.config?.dialpadBeeps == true) {
            toneGenerator.startTone(char)
        }
    }

    fun onKeyPressed(char: Char) {
        playToneAndHaptic(char)
        onAppendChar(char)
    }

    fun onKeyLongPressed(char: Char) {
        if (char == '0') {
            if (dialedNumber.endsWith('0')) {
                onSetNumber(dialedNumber.dropLast(1) + "+")
            } else {
                onAppendChar('+')
            }
        } else if (char.isDigit() && dialedNumber.length <= 1) {
            val speedDial = speedDialValues.firstOrNull { it.id == char.digitToInt() }
            if (speedDial?.isValid() == true) {
                onCallClick(speedDial.number, speedDial.getName(context))
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Matching Contacts List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (matchingContacts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = matchingContacts,
                        key = { it.rawId }
                    ) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val primaryNum = contact.phoneNumbers.firstOrNull()?.normalizedNumber
                                        ?: contact.phoneNumbers.firstOrNull()?.value
                                        ?: dialedNumber
                                    onCallClick(primaryNum, contact.getNameToDisplay())
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ContactAvatar(
                                name = contact.getNameToDisplay(),
                                photoUri = contact.photoUri,
                                size = 42.dp,
                                onClick = { onContactDetailsClick(contact) }
                            )

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 12.dp)
                            ) {
                                HighlightedText(
                                    text = contact.getNameToDisplay(),
                                    highlight = dialedNumber,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1
                                )
                                val number = contact.phoneNumbers.firstOrNull()?.normalizedNumber
                                    ?: contact.phoneNumbers.firstOrNull()?.value.orEmpty()
                                if (number.isNotEmpty()) {
                                    HighlightedText(
                                        text = number,
                                        highlight = dialedNumber,
                                        fontSize = 13.sp,
                                        highlightColor = MaterialTheme.colorScheme.primary,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Middle section: Dialed Number + Add Contact action
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialedNumber.isNotEmpty()) {
                    Text(
                        text = dialedNumber,
                        fontSize = when {
                            dialedNumber.length > 18 -> 20.sp
                            dialedNumber.length > 13 -> 26.sp
                            else -> 34.sp
                        },
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(dialedNumber) {
                                detectTapGestures(
                                    onLongPress = {
                                        clipboardManager.setText(AnnotatedString(dialedNumber))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                    )
                }
            }

            if (dialedNumber.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .clickable { onAddContactClick(dialedNumber) }
                        .padding(top = 2.dp, bottom = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_plus_vector),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.add_number_to_contact),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(29.dp))
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            thickness = 1.dp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        // Keypad Grid (4 Rows x 3 Columns)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    row.forEach { key ->
                        KeypadCell(
                            key = key,
                            onPress = { onKeyPressed(key.char) },
                            onRelease = { toneGenerator.stopTone() },
                            onLongPress = { onKeyLongPressed(key.char) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Bottom Action Row: Spacer | Call button | Backspace
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left slot (empty 56dp box to keep Call button perfectly centered)
            Box(modifier = Modifier.size(56.dp))

            // Center: Call Button
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .size(62.dp)
                    .pointerInput(dialedNumber) {
                        detectTapGestures(
                            onTap = {
                                if (dialedNumber.isNotEmpty()) {
                                    onCallClick(dialedNumber, null)
                                } else {
                                    RecentsHelper(context).getRecentCalls(queryLimit = 1) { recents ->
                                        val mostRecent = recents.firstOrNull()?.phoneNumber
                                        if (!mostRecent.isNullOrEmpty()) {
                                            (context as? Activity)?.runOnUiThread {
                                                onSetNumber(mostRecent)
                                            }
                                        }
                                    }
                                }
                            },
                            onLongPress = {
                                if (dialedNumber.isNotEmpty()) {
                                    onCallWithSimSelector(dialedNumber)
                                }
                            }
                        )
                    }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_phone_vector),
                        contentDescription = stringResource(R.string.call_number),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Right: Backspace Button
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (dialedNumber.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                awaitEachGesture {
                                    val down = awaitFirstDown(requireUnconsumed = false)
                                    down.consume()

                                    var longPressFired = false
                                    val longPressJob = coroutineScope.launch {
                                        delay(viewConfiguration.longPressTimeoutMillis)
                                        longPressFired = true
                                        onClearAll()
                                    }

                                    waitForUpOrCancellation()
                                    longPressJob.cancel()
                                    if (!longPressFired) {
                                        onDeleteChar()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear_outline_vector),
                            contentDescription = "Backspace",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadCell(
    key: KeypadButton,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val viewConfiguration = LocalViewConfiguration.current

    val currentOnPress by rememberUpdatedState(onPress)
    val currentOnRelease by rememberUpdatedState(onRelease)
    val currentOnLongPress by rememberUpdatedState(onLongPress)

    val backgroundColor = if (isPressed) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f)
    }

    Surface(
        shape = RoundedCornerShape(30.dp),
        color = backgroundColor,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(30.dp))
            .pointerInput(key) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    isPressed = true
                    currentOnPress()

                    val longPressJob = coroutineScope.launch {
                        delay(viewConfiguration.longPressTimeoutMillis)
                        currentOnLongPress()
                    }

                    waitForUpOrCancellation()
                    longPressJob.cancel()
                    isPressed = false
                    currentOnRelease()
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = key.char.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (key.letters.isNotEmpty()) {
                Text(
                    text = key.letters,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
private fun ImeiDialog(
    deviceInfo: List<Pair<String, String>>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val hasImei = deviceInfo.any { it.first.startsWith("IMEI") || it.first.startsWith("MEID") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Device Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (!hasImei) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "IMEI is restricted by Android OS for third-party dialers.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    try {
                                        context.startActivity(Intent(Settings.ACTION_DEVICE_INFO_SETTINGS).apply {
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        })
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Could not open Settings", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Open IMEI in Settings",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                deviceInfo.forEach { (label, value) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                clipboardManager.setText(AnnotatedString(value))
                                Toast.makeText(context, "$label copied", Toast.LENGTH_SHORT).show()
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = value,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap to copy",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(org.fossify.commons.R.string.ok),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

private fun getDeviceInfo(context: Context): List<Pair<String, String>> {
    val list = mutableListOf<Pair<String, String>>()
    val tm = context.getSystemService(TelephonyManager::class.java)

    // Try TelephonyManager IMEI / MEID
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val imei0 = tm?.getImei(0)
            if (!imei0.isNullOrBlank()) {
                list.add("IMEI 1" to imei0)
            }
            val imei1 = tm?.getImei(1)
            if (!imei1.isNullOrBlank()) {
                list.add("IMEI 2" to imei1)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val imei0 = tm?.imei
            if (!imei0.isNullOrBlank()) {
                list.add("IMEI" to imei0)
            }
        } else {
            @Suppress("DEPRECATION")
            val deviceId = tm?.deviceId
            if (!deviceId.isNullOrBlank()) {
                list.add("IMEI" to deviceId)
            }
        }
    } catch (_: Exception) {
    }

    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val meid = tm?.meid
            if (!meid.isNullOrBlank()) {
                list.add("MEID" to meid)
            }
        }
    } catch (_: Exception) {
    }

    // Try Subscription SIM details
    try {
        val sm = context.getSystemService(SubscriptionManager::class.java)
        val subs = sm?.activeSubscriptionInfoList
        subs?.forEach { sub ->
            val slot = sub.simSlotIndex + 1
            val carrier = sub.carrierName?.toString()?.takeIf { it.isNotBlank() } ?: "SIM $slot"
            val number = sub.number?.takeIf { it.isNotBlank() }
            if (number != null) {
                list.add("Phone ($carrier)" to number)
            }
            val iccId = sub.iccId?.takeIf { it.isNotBlank() }
            if (iccId != null) {
                list.add("ICCID (SIM $slot)" to iccId)
            }
        }
    } catch (_: Exception) {
    }

    // Try Build Serial
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serial = Build.getSerial()
            if (!serial.isNullOrBlank() && serial != Build.UNKNOWN) {
                list.add("Serial Number" to serial)
            }
        } else {
            @Suppress("DEPRECATION")
            val serial = Build.SERIAL
            if (!serial.isNullOrBlank() && serial != Build.UNKNOWN) {
                list.add("Serial Number" to serial)
            }
        }
    } catch (_: Exception) {
    }

    // Fallback to system properties
    if (list.none { it.first == "Serial Number" }) {
        try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            val serial = (get.invoke(c, "ro.serialno") as? String)
                ?: (get.invoke(c, "ro.boot.serialno") as? String)
            if (!serial.isNullOrBlank()) {
                list.add("Serial Number" to serial)
            }
        } catch (_: Exception) {
        }
    }

    // Device Model & Android Version
    list.add("Device Model" to "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}")
    list.add("Android Version" to "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")

    return list
}
