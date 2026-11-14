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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.commons.helpers.VIEW_TYPE_GRID
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.compose.components.ContactAvatar
import org.fossify.phone.compose.components.FastScroller
import org.fossify.phone.compose.components.HighlightedText

@Composable
fun FavoritesScreen(
    favorites: List<Contact>,
    searchQuery: String,
    viewType: Int,
    columnCount: Int,
    hasPermission: Boolean,
    onContactClick: (Contact) -> Unit,
    onProfileIconClick: (Contact) -> Unit,
    onRequestPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    // Filter favorites based on search query
    val filteredFavorites = remember(favorites, searchQuery) {
        if (searchQuery.isBlank()) {
            favorites
        } else {
            val query = searchQuery.trim().lowercase()
            favorites.filter { contact ->
                contact.getNameToDisplay().lowercase().contains(query) ||
                    contact.phoneNumbers.any { it.value.contains(query) } ||
                    contact.emails.any { it.value.lowercase().contains(query) }
            }.sortedBy { contact ->
                val name = contact.getNameToDisplay().lowercase()
                !name.startsWith(query) && !name.contains(query)
            }
        }
    }

    // Compute letter indexing for FastScroller (when in list mode)
    val (letters, letterIndexMap) = remember(filteredFavorites) {
        val map = mutableMapOf<Char, Int>()
        filteredFavorites.forEachIndexed { index, contact ->
            val firstChar = contact.getNameToDisplay().firstOrNull()?.uppercaseChar() ?: '#'
            val key = if (firstChar in 'A'..'Z') firstChar else '#'
            if (!map.containsKey(key)) {
                map[key] = index
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
        } else if (filteredFavorites.isEmpty()) {
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
            }
        } else if (viewType == VIEW_TYPE_GRID) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(columnCount.coerceAtLeast(1)),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = filteredFavorites,
                    key = { it.rawId }
                ) { contact ->
                    FavoriteGridItem(
                        contact = contact,
                        searchQuery = searchQuery,
                        onContactClick = { onContactClick(contact) },
                        onProfileIconClick = { onProfileIconClick(contact) }
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(
                    items = filteredFavorites,
                    key = { it.rawId }
                ) { contact ->
                    FavoriteListItem(
                        contact = contact,
                        searchQuery = searchQuery,
                        onContactClick = { onContactClick(contact) },
                        onProfileIconClick = { onProfileIconClick(contact) }
                    )
                }
            }

            if (searchQuery.isBlank() && letters.isNotEmpty()) {
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
private fun FavoriteListItem(
    contact: Contact,
    searchQuery: String,
    onContactClick: () -> Unit,
    onProfileIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onContactClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContactAvatar(
            name = contact.getNameToDisplay(),
            photoUri = contact.photoUri,
            size = 46.dp,
            onClick = onProfileIconClick
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
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val number = contact.phoneNumbers.firstOrNull()?.normalizedNumber
                ?: contact.phoneNumbers.firstOrNull()?.value.orEmpty()
            if (number.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                HighlightedText(
                    text = number,
                    highlight = searchQuery,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun FavoriteGridItem(
    contact: Contact,
    searchQuery: String,
    onContactClick: () -> Unit,
    onProfileIconClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onContactClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ContactAvatar(
            name = contact.getNameToDisplay(),
            photoUri = contact.photoUri,
            size = 60.dp,
            onClick = onProfileIconClick
        )

        Spacer(modifier = Modifier.height(6.dp))

        HighlightedText(
            text = contact.getNameToDisplay(),
            highlight = searchQuery,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        val number = contact.phoneNumbers.firstOrNull()?.normalizedNumber
            ?: contact.phoneNumbers.firstOrNull()?.value.orEmpty()
        if (number.isNotEmpty()) {
            HighlightedText(
                text = number,
                highlight = searchQuery,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
