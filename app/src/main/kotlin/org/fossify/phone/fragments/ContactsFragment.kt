package org.fossify.phone.fragments

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getMyContactsCursor
import org.fossify.commons.extensions.hasPermission
import org.fossify.commons.helpers.ContactsHelper
import org.fossify.commons.helpers.MyContactsContentProvider
import org.fossify.commons.helpers.PERMISSION_READ_CONTACTS
import org.fossify.commons.helpers.SMT_PRIVATE
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.activities.MainActivity
import org.fossify.phone.compose.screens.ContactsScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.extensions.handleGenericContactClick
import org.fossify.phone.extensions.launchCreateNewContactIntent
import org.fossify.phone.extensions.startContactDetailsIntent
import org.fossify.phone.helpers.DebugContactsHelper
import org.fossify.phone.helpers.debugMode
import org.fossify.phone.interfaces.RefreshItemsListener

class ContactsFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.ComposeInnerBinding>(context, attributeSet),
    RefreshItemsListener {

    private var contactsState by mutableStateOf<List<Contact>>(emptyList())
    private var favoritesState by mutableStateOf<List<Contact>>(emptyList())
    private var searchQueryState by mutableStateOf("")
    private var hasPermissionState by mutableStateOf(false)
    private var composeView: ComposeView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        innerBinding = ComposeInnerBinding()
        composeView = findViewById(R.id.compose_view)
    }

    override fun setupFragment() {
        hasPermissionState = context.hasPermission(PERMISSION_READ_CONTACTS)

        composeView?.setContent {
            PhoneTheme(activity = activity) {
                ContactsScreen(
                    contacts = contactsState,
                    favorites = favoritesState,
                    searchQuery = searchQueryState,
                    hasPermission = hasPermissionState,
                    onContactClick = { contact ->
                        activity?.handleGenericContactClick(contact)
                    },
                    onProfileIconClick = { contact ->
                        activity?.startContactDetailsIntent(contact)
                    },
                    onCreateContactClick = {
                        activity?.launchCreateNewContactIntent()
                    },
                    onRequestPermissionClick = {
                        requestReadContactsPermission()
                    }
                )
            }
        }

        refreshItems()
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        // Handled reactively by Compose PhoneTheme
    }

    override fun refreshItems(invalidate: Boolean, callback: (() -> Unit)?) {
        hasPermissionState = context.hasPermission(PERMISSION_READ_CONTACTS)
        if (!hasPermissionState) {
            contactsState = emptyList()
            favoritesState = emptyList()
            callback?.invoke()
            return
        }

        val privateCursor = context?.getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            val allContacts = ArrayList(contacts)

            if (SMT_PRIVATE !in context.baseConfig.ignoredContactSources) {
                val privateContacts = MyContactsContentProvider.getContacts(context, privateCursor)
                if (privateContacts.isNotEmpty()) {
                    allContacts.addAll(privateContacts)
                    allContacts.sort()
                }
            }

            if (debugMode && allContacts.isEmpty()) {
                DebugContactsHelper.insertSampleContactsIfEmpty(context) {
                    ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { reloaded ->
                        (activity as? MainActivity)?.cachedContacts = ArrayList(reloaded)
                        updateContactsAndFavorites(reloaded)
                        callback?.invoke()
                    }
                }
            } else {
                (activity as? MainActivity)?.cachedContacts = allContacts
                updateContactsAndFavorites(allContacts)
                callback?.invoke()
            }
        }
    }

    private fun updateContactsAndFavorites(contacts: List<Contact>) {
        var favs = contacts.filter { it.starred == 1 }

        if (SMT_PRIVATE !in context.baseConfig.ignoredContactSources) {
            val privateFavCursor = context?.getMyContactsCursor(favoritesOnly = true, withPhoneNumbersOnly = true)
            val privateFavs = MyContactsContentProvider.getContacts(context, privateFavCursor).map {
                it.copy(starred = 1)
            }
            if (privateFavs.isNotEmpty()) {
                favs = (favs + privateFavs).distinctBy { it.contactId }
            }
        }

        if (debugMode && favs.isEmpty() && contacts.isNotEmpty()) {
            favs = contacts.take(5)
        }

        post {
            favoritesState = favs
            contactsState = contacts
        }
    }

    override fun onSearchClosed() {
        searchQueryState = ""
    }

    override fun onSearchQueryChanged(text: String) {
        searchQueryState = text
    }

    private fun requestReadContactsPermission() {
        activity?.handlePermission(PERMISSION_READ_CONTACTS) { granted ->
            hasPermissionState = granted
            if (granted) {
                refreshItems()
            }
        }
    }
}
