package org.fossify.phone.compose.screens

import android.provider.CallLog.Calls
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.commons.extensions.formatDateOrTime
import org.fossify.commons.extensions.formatSecondsToShortTimeString
import org.fossify.commons.extensions.formatTime
import org.fossify.phone.R
import org.fossify.phone.compose.components.ContactAvatar
import org.fossify.phone.compose.theme.CallIncoming
import org.fossify.phone.compose.theme.CallMissed
import org.fossify.phone.compose.theme.CallOutgoing
import org.fossify.phone.compose.components.HighlightedText
import org.fossify.phone.extensions.getDayCode
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.RecentCall
import org.joda.time.DateTime

@Composable
fun RecentsScreen(
    items: List<CallLogItem>,
    searchQuery: String,
    hasPermission: Boolean,
    onCallClick: (RecentCall) -> Unit,
    onProfileClick: (RecentCall) -> Unit,
    onDeleteCall: (RecentCall) -> Unit,
    onSendSms: (String) -> Unit,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) {
            items
        } else {
            val query = searchQuery.trim().lowercase()
            items.filter { item ->
                when (item) {
                    is RecentCall -> {
                        item.name.lowercase().contains(query) ||
                            item.doesContainPhoneNumber(query)
                    }
                    is CallLogItem.Date -> false
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (!hasPermission) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.could_not_access_the_call_history),
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onRequestPermissionClick) {
                    Text(
                        text = stringResource(R.string.request_access),
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else if (filteredItems.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_previous_calls),
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = filteredItems,
                    key = { it.getItemId() }
                ) { item ->
                    when (item) {
                        is CallLogItem.Date -> {
                            DateHeader(date = item)
                        }
                        is RecentCall -> {
                            RecentCallListItem(
                                call = item,
                                searchQuery = searchQuery,
                                onClick = { onCallClick(item) },
                                onProfileClick = { onProfileClick(item) },
                                onDelete = { onDeleteCall(item) },
                                onSendSms = { onSendSms(item.phoneNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateHeader(date: CallLogItem.Date) {
    val context = LocalContext.current
    val now = remember { DateTime.now() }
    val dateText = remember(date, now) {
        when (date.dayCode) {
            now.millis.getDayCode() -> context.getString(R.string.today)
            now.minusDays(1).millis.getDayCode() -> context.getString(R.string.yesterday)
            else -> date.timestamp.formatDateOrTime(
                context = context,
                hideTimeOnOtherDays = true,
                showCurrentYear = false
            )
        }
    }

    Text(
        text = dateText,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 6.dp)
    )
}

@Composable
private fun RecentCallListItem(
    call: RecentCall,
    searchQuery: String,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    onDelete: () -> Unit,
    onSendSms: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showMenu by remember { mutableStateOf(false) }

    val isMissed = call.type == Calls.MISSED_TYPE || call.type == Calls.REJECTED_TYPE
    val callIconRes = when (call.type) {
        Calls.OUTGOING_TYPE -> R.drawable.ic_call_made_vector
        Calls.MISSED_TYPE -> R.drawable.ic_call_missed_vector
        else -> R.drawable.ic_call_received_vector
    }

    val callTypeColor = when (call.type) {
        Calls.OUTGOING_TYPE -> CallOutgoing
        Calls.MISSED_TYPE, Calls.REJECTED_TYPE -> CallMissed
        else -> CallIncoming
    }

    val displayName = if (call.name.isNotEmpty()) call.name else call.phoneNumber
    val countSuffix = if (!call.groupedCalls.isNullOrEmpty()) " (${call.groupedCalls.size + 1})" else ""
    val fullDisplayName = "$displayName$countSuffix"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = displayName,
            photoUri = call.photoUri,
            size = 44.dp,
            onClick = onProfileClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp, end = 8.dp)
        ) {
            HighlightedText(
                text = fullDisplayName,
                highlight = searchQuery,
                fontSize = 16.sp,
                maxLines = 1,
                highlightColor = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(callIconRes),
                    contentDescription = null,
                    tint = callTypeColor,
                    modifier = Modifier.size(13.dp)
                )

                Spacer(modifier = Modifier.width(4.dp))

                val formattedTime = remember(call.startTS) { call.startTS.formatTime(context) }
                Text(
                    text = formattedTime,
                    fontSize = 12.sp,
                    color = if (isMissed) CallMissed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )

                if (!isMissed && call.duration > 0) {
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = context.formatSecondsToShortTimeString(call.duration),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }

                if (call.simID != -1 && call.simID != 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(3.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        Text(
                            text = "SIM ${call.simID}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }

        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    painter = painterResource(org.fossify.commons.R.drawable.ic_three_dots_vector),
                    contentDescription = stringResource(org.fossify.commons.R.string.more_options),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(org.fossify.commons.R.string.call_contact)) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.send_sms)) },
                    onClick = {
                        showMenu = false
                        onSendSms()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.view_contact_details)) },
                    onClick = {
                        showMenu = false
                        onProfileClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(org.fossify.commons.R.string.delete)) },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}
