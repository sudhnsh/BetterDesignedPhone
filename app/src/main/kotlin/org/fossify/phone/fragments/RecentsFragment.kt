package org.fossify.phone.fragments

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import org.fossify.commons.extensions.hasPermission
import org.fossify.commons.extensions.launchSendSMSIntent
import org.fossify.commons.helpers.PERMISSION_READ_CALL_LOG
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.activities.MainActivity
import org.fossify.phone.compose.screens.RecentsScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.extensions.config
import org.fossify.phone.extensions.startAddContactIntent
import org.fossify.phone.extensions.startCallWithConfirmationCheck
import org.fossify.phone.extensions.startContactDetailsIntent
import org.fossify.phone.helpers.DebugContactsHelper
import org.fossify.phone.helpers.RecentsHelper
import org.fossify.phone.helpers.debugMode
import org.fossify.phone.interfaces.RefreshItemsListener
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.RecentCall

class RecentsFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.ComposeInnerBinding>(context, attributeSet),
    RefreshItemsListener {

    private var itemsState by mutableStateOf<List<CallLogItem>>(emptyList())
    private var searchQueryState by mutableStateOf("")
    private var hasPermissionState by mutableStateOf(false)
    private var composeView: ComposeView? = null
    private var recentsHelper = RecentsHelper(context)

    override fun onFinishInflate() {
        super.onFinishInflate()
        innerBinding = ComposeInnerBinding()
        composeView = findViewById(R.id.compose_view)
    }

    override fun setupFragment() {
        hasPermissionState = context.hasPermission(PERMISSION_READ_CALL_LOG)

        composeView?.setContent {
            PhoneTheme(activity = activity) {
                RecentsScreen(
                    items = itemsState,
                    searchQuery = searchQueryState,
                    hasPermission = hasPermissionState,
                    onCallClick = { recentCall ->
                        activity?.startCallWithConfirmationCheck(recentCall.phoneNumber, recentCall.name)
                    },
                    onProfileClick = { recentCall ->
                        val contact = findContactByCall(recentCall)
                        if (contact != null) {
                            activity?.startContactDetailsIntent(contact)
                        } else {
                            activity?.startAddContactIntent(recentCall.phoneNumber)
                        }
                    },
                    onDeleteCall = { recentCall ->
                        itemsState = itemsState.filter { (it as? RecentCall)?.id != recentCall.id }
                        recentsHelper.removeRecentCalls(listOf(recentCall.id)) {}
                    },
                    onSendSms = { number ->
                        activity?.launchSendSMSIntent(number)
                    },
                    onRequestPermissionClick = {
                        requestCallLogPermission()
                    }
                )
            }
        }

        refreshItems()
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        // Managed reactively by PhoneTheme
    }

    override fun refreshItems(invalidate: Boolean, callback: (() -> Unit)?) {
        hasPermissionState = context.hasPermission(PERMISSION_READ_CALL_LOG)
        if (!hasPermissionState) {
            itemsState = emptyList()
            callback?.invoke()
            return
        }

        getRecentCalls(loadAll = true) { calls ->
            post {
                itemsState = if (debugMode && calls.isEmpty()) {
                    DebugContactsHelper.getSampleCallLogs()
                } else {
                    calls
                }
                callback?.invoke()
            }
        }
    }

    override fun onSearchClosed() {
        searchQueryState = ""
    }

    override fun onSearchQueryChanged(text: String) {
        searchQueryState = text
    }

    private fun findContactByCall(recentCall: RecentCall): Contact? {
        return (activity as? MainActivity)?.cachedContacts?.find {
            it.name == recentCall.name && it.doesHavePhoneNumber(recentCall.phoneNumber)
        }
    }

    private fun requestCallLogPermission() {
        activity?.handlePermission(PERMISSION_READ_CALL_LOG) { granted ->
            hasPermissionState = granted
            if (granted) {
                refreshItems()
            }
        }
    }

    private fun getRecentCalls(loadAll: Boolean = false, callback: (List<CallLogItem>) -> Unit) {
        val queryLimit = if (loadAll) -1 else 40
        recentsHelper.getRecentCalls(queryLimit = queryLimit) { calls ->
            prepareCallLog(calls) { prepared ->
                callback(prepared)
            }
        }
    }

    private fun prepareCallLog(recentCalls: List<RecentCall>, callback: (List<CallLogItem>) -> Unit) {
        val callLogItems = mutableListOf<CallLogItem>()
        var currentDayCode = ""

        recentCalls.forEach { call ->
            if (call.dayCode != currentDayCode) {
                currentDayCode = call.dayCode
                callLogItems.add(CallLogItem.Date(call.startTS, call.dayCode))
            }
            callLogItems.add(call)
        }

        callback(callLogItems)
    }
}
