package org.fossify.phone.compose.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.compose.components.ContactAvatar
import org.fossify.phone.compose.components.FastScroller
import org.fossify.phone.compose.components.HighlightedText

@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    favorites: List<Contact> = emptyList(),
    searchQuery: String,
    hasPermission: Boolean,
    onContactClick: (Contact) -> Unit,
    onProfileIconClick: (Contact) -> Unit,
    onCreateContactClick: () -> Unit,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Filter contacts based on search query
    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isBlank()) {
            contacts
        } else {
            val query = searchQuery.trim().lowercase()
            contacts.filter { contact ->
                contact.getNameToDisplay().lowercase().contains(query) ||
                    contact.phoneNumbers.any { it.value.contains(query) } ||
                    contact.emails.any { it.value.lowercase().contains(query) }
            }.sortedBy { contact ->
                val name = contact.getNameToDisplay().lowercase()
                !name.startsWith(query) && !name.contains(query)
            }
        }
    }

    // Compute letter indexing for FastScroller
    val showFavorites = searchQuery.isBlank() && favorites.isNotEmpty()
    val (letters, letterIndexMap) = remember(filteredContacts, showFavorites) {
        val map = mutableMapOf<Char, Int>()
        val offset = if (showFavorites) 1 else 0
        filteredContacts.forEachIndexed { index, contact ->
            val firstChar = contact.getNameToDisplay().firstOrNull()?.uppercaseChar() ?: '#'
            val key = if (firstChar in 'A'..'Z') firstChar else '#'
            if (!map.containsKey(key)) {
                map[key] = index + offset
            }
        }
        val distinctLetters = map.keys.sortedWith { a, b ->
            when {
                a == '#' -> 1
                b == '#' -> -1
                else -> a.compareTo(b)
            }
        }
        distinctLetters to map
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
                    text = stringResource(R.string.could_not_access_contacts),
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
        } else if (filteredContacts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_contacts_found),
                    fontStyle = FontStyle.Italic,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onCreateContactClick) {
                    Text(
                        text = stringResource(R.string.create_new_contact),
                        fontSize = 16.sp,
                        textDecoration = TextDecoration.Underline,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                if (showFavorites) {
                    item(key = "favorites_top_section") {
                        FavoritesTopSection(
                            favorites = favorites,
                            onContactClick = onContactClick,
                            onProfileIconClick = onProfileIconClick
                        )
                    }
                }

                items(
                    items = filteredContacts,
                    key = { it.rawId }
                ) { contact ->
                    ContactListItem(
                        contact = contact,
                        searchQuery = searchQuery,
                        onClick = { onContactClick(contact) },
                        onProfileClick = { onProfileIconClick(contact) }
                    )
                }
            }

            if (letters.size > 1 && searchQuery.isEmpty()) {
                FastScroller(
                    letters = letters,
                    listState = listState,
                    letterIndexMap = letterIndexMap,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}

@Composable
private fun FavoritesTopSection(
    favorites: List<Contact>,
    onContactClick: (Contact) -> Unit,
    onProfileIconClick: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_star_vector),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = stringResource(org.fossify.commons.R.string.favorites).uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = favorites,
                key = { "fav_${it.rawId}" }
            ) { contact ->
                FavoriteContactChip(
                    contact = contact,
                    onClick = { onContactClick(contact) },
                    onProfileIconClick = { onProfileIconClick(contact) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
            thickness = 1.dp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.all_contacts).uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun FavoriteContactChip(
    contact: Contact,
    onClick: () -> Unit,
    onProfileIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(64.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            ContactAvatar(
                name = contact.getNameToDisplay(),
                photoUri = contact.photoUri,
                size = 54.dp,
                onClick = onProfileIconClick
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val firstName = remember(contact.getNameToDisplay()) {
            contact.getNameToDisplay().trim().split("\\s+".toRegex()).firstOrNull() ?: contact.getNameToDisplay()
        }

        Text(
            text = firstName,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ContactListItem(
    contact: Contact,
    searchQuery: String,
    onClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = contact.getNameToDisplay(),
            photoUri = contact.photoUri,
            size = 46.dp,
            onClick = onProfileClick
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, end = 8.dp)
        ) {
            HighlightedText(
                text = contact.getNameToDisplay(),
                highlight = searchQuery,
                fontSize = 16.sp,
                maxLines = 1
            )

            val primaryNumber = contact.phoneNumbers.firstOrNull()?.normalizedNumber
                ?: contact.phoneNumbers.firstOrNull()?.value
            if (!primaryNumber.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = primaryNumber,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
