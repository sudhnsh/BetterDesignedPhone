package org.fossify.phone.compose.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import org.fossify.phone.compose.components.PhoneBottomNavBar
import org.fossify.phone.compose.components.defaultPhoneNavBarItems
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.RecentCall
import org.fossify.phone.models.SpeedDial

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    initialTab: Int = 0,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {},
    // Recents
    recents: List<CallLogItem>,
    hasCallLogPermission: Boolean,
    onCallClick: (RecentCall) -> Unit,
    onRecentProfileClick: (RecentCall) -> Unit,
    onDeleteRecentCall: (RecentCall) -> Unit,
    onSendSms: (String) -> Unit,
    onRequestCallLogPermission: () -> Unit,
    onClearCallHistory: () -> Unit,
    // Contacts
    contacts: List<Contact>,
    favorites: List<Contact>,
    hasContactsPermission: Boolean,
    onContactClick: (Contact) -> Unit,
    onContactProfileClick: (Contact) -> Unit,
    onCreateContactClick: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onDeleteContacts: (List<Int>) -> Unit = {},
    // Keypad
    dialedNumber: String,
    onAppendChar: (Char) -> Unit,
    onDeleteChar: () -> Unit,
    onClearAllDialed: () -> Unit,
    onSetDialedNumber: (String) -> Unit,
    speedDialValues: List<SpeedDial>,
    onDialpadCallClick: (String, String?) -> Unit,
    onDialpadCallWithSimSelector: (String) -> Unit,
    onDialpadAddContactClick: (String) -> Unit,
    showImeiDialog: Boolean,
    onDismissImeiDialog: () -> Unit,
    // Navigation & Menus
    onTabSelected: (Int) -> Unit = {},
    onSettingsClick: () -> Unit,
    onAboutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(initialPage = initialTab.coerceIn(0, 2)) { 3 }
    val coroutineScope = rememberCoroutineScope()
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showClearCallHistoryDialog by remember { mutableStateOf(false) }
    var selectedContactIds by remember { mutableStateOf(setOf<Int>()) }
    var showDeleteContactsConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(initialTab) {
        if (initialTab in 0..2 && pagerState.currentPage != initialTab) {
            pagerState.scrollToPage(initialTab)
        }
    }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != 1 && selectedContactIds.isNotEmpty()) {
            selectedContactIds = emptySet()
        }
    }

    BackHandler(enabled = selectedContactIds.isNotEmpty()) {
        selectedContactIds = emptySet()
    }

    // Reset search when swiping/changing tabs
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            onTabSelected(page)
            if (isSearchActive) {
                onSearchActiveChange(false)
            }
            if (searchQuery.isNotEmpty()) {
                onSearchQueryChange("")
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            if (selectedContactIds.isNotEmpty() && pagerState.currentPage == 1) {
                TopAppBar(
                    title = {
                        Text(
                            text = "${selectedContactIds.size} selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedContactIds = emptySet() }) {
                            Icon(
                                painter = painterResource(org.fossify.commons.R.drawable.ic_cross_vector),
                                contentDescription = stringResource(org.fossify.commons.R.string.cancel)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = TopAppBarDefaults.windowInsets,
                    actions = {
                        IconButton(onClick = {
                            selectedContactIds = if (selectedContactIds.size == contacts.size) {
                                emptySet()
                            } else {
                                contacts.map { it.rawId }.toSet()
                            }
                        }) {
                            Icon(
                                painter = painterResource(org.fossify.commons.R.drawable.ic_select_all_vector),
                                contentDescription = stringResource(org.fossify.commons.R.string.select_all)
                            )
                        }
                        IconButton(onClick = { showDeleteContactsConfirmation = true }) {
                            Icon(
                                painter = painterResource(org.fossify.commons.R.drawable.ic_delete_vector),
                                contentDescription = stringResource(org.fossify.commons.R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            } else if (!isSearchActive) {
                TopAppBar(
                    title = {
                        Text(
                            text = when (pagerState.currentPage) {
                                0 -> stringResource(R.string.tab_call)
                                1 -> stringResource(R.string.contacts_tab)
                                else -> stringResource(R.string.tab_keypad)
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = TopAppBarDefaults.windowInsets,
                    actions = {
                        // Keypad screen does NOT need a search button (only Call & Contacts)
                        if (pagerState.currentPage != 2) {
                            IconButton(onClick = { onSearchActiveChange(true) }) {
                                Icon(
                                    painter = painterResource(org.fossify.commons.R.drawable.ic_search_vector),
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                        }

                        // Tab-specific action icons
                        when (pagerState.currentPage) {
                            1 -> {
                                IconButton(onClick = onCreateContactClick) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_plus_vector),
                                        contentDescription = stringResource(R.string.create_new_contact)
                                    )
                                }
                            }
                        }

                        // Overflow Menu Button
                        Box {
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(
                                    painter = painterResource(org.fossify.commons.R.drawable.ic_three_dots_vector),
                                    contentDescription = stringResource(org.fossify.commons.R.string.more_options)
                                )
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false },
                                shape = RoundedCornerShape(20.dp),
                                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                tonalElevation = 6.dp,
                                shadowElevation = 10.dp,
                                modifier = Modifier
                                    .widthIn(min = 220.dp)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                if (pagerState.currentPage == 0) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.clear_call_history),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_delete_vector),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            showClearCallHistoryDialog = true
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }

                                if (pagerState.currentPage == 1) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.sort_by),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_sort_vector),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onSortClick()
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = stringResource(R.string.filter),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            Icon(
                                                painter = painterResource(R.drawable.ic_filter_vector),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        },
                                        onClick = {
                                            showOverflowMenu = false
                                            onFilterClick()
                                        },
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                    )
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                }

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.settings),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_settings_cog_vector),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onSettingsClick()
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )

                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(R.string.about),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_info_vector),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        onAboutClick()
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!isSearchActive) {
                val navItems = remember { defaultPhoneNavBarItems() }
                PhoneBottomNavBar(
                    items = navItems,
                    selectedIndex = pagerState.currentPage,
                    onItemSelected = { page ->
                        onSearchActiveChange(false)
                        if (searchQuery.isNotEmpty()) {
                            onSearchQueryChange("")
                        }
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        if (isSearchActive) {
            SearchScreen(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onCloseSearch = {
                    onSearchActiveChange(false)
                    onSearchQueryChange("")
                },
                allContacts = contacts,
                allRecentCalls = recents,
                onContactClick = onContactClick,
                onContactProfileClick = onContactProfileClick,
                onCallClick = onCallClick,
                onCallProfileClick = onRecentProfileClick,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            )
        } else {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) { page ->
                when (page) {
                    0 -> {
                        RecentsScreen(
                            items = recents,
                            searchQuery = searchQuery,
                            hasPermission = hasCallLogPermission,
                            onCallClick = onCallClick,
                            onProfileClick = onRecentProfileClick,
                            onDeleteCall = onDeleteRecentCall,
                            onSendSms = onSendSms,
                            onRequestPermissionClick = onRequestCallLogPermission
                        )
                    }
                    1 -> {
                        ContactsScreen(
                            contacts = contacts,
                            favorites = favorites,
                            searchQuery = searchQuery,
                            hasPermission = hasContactsPermission,
                            selectedContactIds = selectedContactIds,
                            onToggleSelectContact = { contact ->
                                selectedContactIds = if (selectedContactIds.contains(contact.rawId)) {
                                    selectedContactIds - contact.rawId
                                } else {
                                    selectedContactIds + contact.rawId
                                }
                            },
                            onContactLongClick = { contact ->
                                selectedContactIds = if (selectedContactIds.contains(contact.rawId)) {
                                    selectedContactIds - contact.rawId
                                } else {
                                    selectedContactIds + contact.rawId
                                }
                            },
                            onContactClick = onContactClick,
                            onProfileIconClick = onContactProfileClick,
                            onCreateContactClick = onCreateContactClick,
                            onRequestPermissionClick = onRequestContactsPermission
                        )
                    }
                    2 -> {
                        DialpadScreen(
                            dialedNumber = dialedNumber,
                            onAppendChar = onAppendChar,
                            onDeleteChar = onDeleteChar,
                            onClearAll = onClearAllDialed,
                            onSetNumber = onSetDialedNumber,
                            allContacts = contacts,
                            speedDialValues = speedDialValues,
                            onCallClick = onDialpadCallClick,
                            onCallWithSimSelector = onDialpadCallWithSimSelector,
                            onContactDetailsClick = onContactClick,
                            onAddContactClick = onDialpadAddContactClick,
                            showImeiDialog = showImeiDialog,
                            onDismissImeiDialog = onDismissImeiDialog
                        )
                    }
                }
            }
        }
    }

    if (showClearCallHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearCallHistoryDialog = false },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            icon = {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete_vector),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(26.dp)
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(R.string.clear_call_history),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.clear_history_confirmation) + "\n\n" + stringResource(R.string.cannot_be_undone),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearCallHistoryDialog = false
                        onClearCallHistory()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(R.string.clear),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showClearCallHistoryDialog = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(org.fossify.commons.R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        )
    }

    if (showDeleteContactsConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteContactsConfirmation = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            title = {
                Text(
                    text = if (selectedContactIds.size == 1) "Delete Contact" else "Delete Contacts",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = if (selectedContactIds.size == 1) {
                        "Are you sure you want to delete the selected contact?"
                    } else {
                        "Are you sure you want to delete ${selectedContactIds.size} selected contacts?"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val idsToDelete = selectedContactIds.toList()
                        selectedContactIds = emptySet()
                        showDeleteContactsConfirmation = false
                        onDeleteContacts(idsToDelete)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(org.fossify.commons.R.string.delete),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteContactsConfirmation = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = stringResource(org.fossify.commons.R.string.cancel),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        )
    }
}
