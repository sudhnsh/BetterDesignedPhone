package org.fossify.phone.compose.screens

import android.provider.CallLog.Calls
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.commons.extensions.formatTime
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.compose.components.ContactAvatar
import org.fossify.phone.compose.components.HighlightedText
import org.fossify.phone.compose.theme.CallIncoming
import org.fossify.phone.compose.theme.CallMissed
import org.fossify.phone.compose.theme.CallOutgoing
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.RecentCall

@Composable
fun SearchScreen(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    allContacts: List<Contact>,
    allRecentCalls: List<CallLogItem>,
    onContactClick: (Contact) -> Unit,
    onContactProfileClick: (Contact) -> Unit,
    onCallClick: (RecentCall) -> Unit,
    onCallProfileClick: (RecentCall) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    BackHandler {
        onCloseSearch()
    }

    // Filter contacts based on query
    val filteredContacts = remember(allContacts, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim().lowercase()
            allContacts.filter { contact ->
                contact.getNameToDisplay().lowercase().contains(query) ||
                    contact.phoneNumbers.any { it.value.contains(query) } ||
                    contact.emails.any { it.value.lowercase().contains(query) }
            }.sortedBy { contact ->
                val name = contact.getNameToDisplay().lowercase()
                !name.startsWith(query) && !name.contains(query)
            }
        }
    }

    // Filter recent calls based on query
    val filteredCalls = remember(allRecentCalls, searchQuery) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val query = searchQuery.trim().lowercase()
            allRecentCalls.filterIsInstance<RecentCall>().filter { call ->
                call.name.lowercase().contains(query) ||
                    call.doesContainPhoneNumber(query)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
    ) {
        // Top Search Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCloseSearch) {
                    Icon(
                        painter = painterResource(org.fossify.commons.R.drawable.ic_arrow_left_vector),
                        contentDescription = "Close search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search contacts and calls...",
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                }

                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_clear_outline_vector),
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Search Results Body
        if (searchQuery.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(org.fossify.commons.R.drawable.ic_search_vector),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Type a name or phone number",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else if (filteredContacts.isEmpty() && filteredCalls.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No contacts or calls found",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Section 1: Contacts
                if (filteredContacts.isNotEmpty()) {
                    item(key = "header_contacts") {
                        Text(
                            text = "CONTACTS (${filteredContacts.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredContacts, key = { "contact_${it.id}" }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onContactClick(contact) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.clickable { onContactProfileClick(contact) }) {
                                ContactAvatar(name = contact.getNameToDisplay(), photoUri = contact.photoUri, size = 44.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HighlightedText(
                                    text = contact.getNameToDisplay(),
                                    highlight = searchQuery,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val primaryNumber = contact.phoneNumbers.firstOrNull()?.value
                                if (!primaryNumber.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    HighlightedText(
                                        text = primaryNumber,
                                        highlight = searchQuery,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            IconButton(onClick = { onContactClick(contact) }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_phone_vector),
                                    contentDescription = "Call contact",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }

                // Divider between sections if both have results
                if (filteredContacts.isNotEmpty() && filteredCalls.isNotEmpty()) {
                    item(key = "divider_sections") {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        )
                    }
                }

                // Section 2: Calls (Recent Calls)
                if (filteredCalls.isNotEmpty()) {
                    item(key = "header_calls") {
                        Text(
                            text = "CALL HISTORY (${filteredCalls.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(filteredCalls, key = { "call_${it.id}" }) { call ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCallClick(call) }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.clickable { onCallProfileClick(call) }) {
                                ContactAvatar(name = call.name.ifEmpty { call.phoneNumber }, photoUri = call.photoUri, size = 44.dp)
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                HighlightedText(
                                    text = call.name.ifEmpty { call.phoneNumber },
                                    highlight = searchQuery,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (call.type == Calls.MISSED_TYPE) CallMissed else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val (callIcon, callColor) = when (call.type) {
                                        Calls.INCOMING_TYPE -> R.drawable.ic_call_received_vector to CallIncoming
                                        Calls.OUTGOING_TYPE -> R.drawable.ic_call_made_vector to CallOutgoing
                                        Calls.MISSED_TYPE, Calls.REJECTED_TYPE -> R.drawable.ic_call_missed_vector to CallMissed
                                        else -> R.drawable.ic_call_received_vector to CallIncoming
                                    }
                                    Icon(
                                        painter = painterResource(callIcon),
                                        contentDescription = null,
                                        tint = callColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    if (call.name.isNotEmpty() && call.phoneNumber.isNotEmpty()) {
                                        HighlightedText(
                                            text = call.phoneNumber,
                                            highlight = searchQuery,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = " • ",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                    Text(
                                        text = call.startTS.formatTime(context),
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                            IconButton(onClick = { onCallClick(call) }) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_phone_vector),
                                    contentDescription = "Call",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
