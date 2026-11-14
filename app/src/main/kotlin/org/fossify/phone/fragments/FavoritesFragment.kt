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
import org.fossify.commons.helpers.Converters
import org.fossify.commons.helpers.MyContactsContentProvider
import org.fossify.commons.helpers.PERMISSION_READ_CONTACTS
import org.fossify.commons.helpers.SMT_PRIVATE
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.compose.screens.FavoritesScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.extensions.config
import org.fossify.phone.extensions.handleGenericContactClick
import org.fossify.phone.extensions.startContactDetailsIntent
import org.fossify.phone.interfaces.RefreshItemsListener

class FavoritesFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.ComposeInnerBinding>(context, attributeSet),
    RefreshItemsListener {

    private var favoritesState by mutableStateOf<List<Contact>>(emptyList())
    private var searchQueryState by mutableStateOf("")
    private var viewTypeState by mutableStateOf(0)
    private var columnCountState by mutableStateOf(3)
    private var hasPermissionState by mutableStateOf(false)
    private var composeView: ComposeView? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        innerBinding = ComposeInnerBinding()
        composeView = findViewById(R.id.compose_view)
    }

    override fun setupFragment() {
        val act = activity ?: return
        hasPermissionState = context.hasPermission(PERMISSION_READ_CONTACTS)
        viewTypeState = act.config.viewType
        columnCountState = act.config.contactsGridColumnCount

        composeView?.setContent {
            PhoneTheme(activity = activity) {
                FavoritesScreen(
                    favorites = favoritesState,
                    searchQuery = searchQueryState,
                    viewType = viewTypeState,
                    columnCount = columnCountState,
                    hasPermission = hasPermissionState,
                    onContactClick = { contact ->
                        activity?.handleGenericContactClick(contact)
                    },
                    onProfileIconClick = { contact ->
                        activity?.startContactDetailsIntent(contact)
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
        val act = activity ?: return
        hasPermissionState = context.hasPermission(PERMISSION_READ_CONTACTS)
        viewTypeState = act.config.viewType
        columnCountState = act.config.contactsGridColumnCount

        if (!hasPermissionState) {
            favoritesState = emptyList()
            callback?.invoke()
            return
        }

        ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            val allContacts = ArrayList(contacts)

            if (SMT_PRIVATE !in context.baseConfig.ignoredContactSources) {
                val privateCursor = context?.getMyContactsCursor(favoritesOnly = true, withPhoneNumbersOnly = true)
                val privateContacts = MyContactsContentProvider.getContacts(context, privateCursor).map {
                    it.copy(starred = 1)
                }
                if (privateContacts.isNotEmpty()) {
                    allContacts.addAll(privateContacts)
                    allContacts.sort()
                }
            }

            val favorites = allContacts.filter { it.starred == 1 }
            val sortedFavorites = if (act.config.isCustomOrderSelected) {
                sortByCustomOrder(favorites)
            } else {
                favorites
            }

            act.runOnUiThread {
                favoritesState = sortedFavorites
                callback?.invoke()
            }
        }
    }

    fun columnCountChanged() {
        columnCountState = context.config.contactsGridColumnCount
    }

    private fun sortByCustomOrder(favorites: List<Contact>): List<Contact> {
        val favoritesOrder = activity?.config?.favoritesContactsOrder.orEmpty()
        if (favoritesOrder.isEmpty()) {
            return favorites
        }

        val orderList = Converters().jsonToStringList(favoritesOrder)
        val map = orderList.withIndex().associate { it.value to it.index }
        return favorites.sortedBy { map[it.contactId.toString()] }
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
