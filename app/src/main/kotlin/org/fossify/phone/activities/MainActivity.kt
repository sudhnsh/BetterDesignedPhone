package org.fossify.phone.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import org.fossify.commons.dialogs.ConfirmationDialog
import org.fossify.commons.extensions.*
import org.fossify.commons.helpers.*
import org.fossify.commons.models.FAQItem
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.BuildConfig
import org.fossify.phone.R
import org.fossify.phone.compose.screens.MainScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.dialogs.ChangeSortingDialog
import org.fossify.phone.dialogs.FilterContactSourcesDialog
import org.fossify.phone.extensions.areMultipleSIMsAvailable
import org.fossify.phone.extensions.clearMissedCalls
import org.fossify.phone.extensions.config
import org.fossify.phone.extensions.handleFullScreenNotificationsPermission
import org.fossify.phone.extensions.handleGenericContactClick
import org.fossify.phone.extensions.launchCreateNewContactIntent
import org.fossify.phone.extensions.startAddContactIntent
import org.fossify.phone.extensions.startCallWithConfirmationCheck
import org.fossify.phone.extensions.startContactDetailsIntent
import org.fossify.phone.helpers.DebugContactsHelper
import org.fossify.phone.helpers.OPEN_DIAL_PAD_AT_LAUNCH
import org.fossify.phone.helpers.RecentsHelper
import org.fossify.phone.helpers.TAB_DIALPAD
import org.fossify.phone.helpers.debugMode
import org.fossify.phone.helpers.tabsList
import org.fossify.phone.models.CallLogItem
import org.fossify.phone.models.Events
import org.fossify.phone.models.RecentCall
import org.fossify.phone.models.SpeedDial
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MainActivity : SimpleActivity() {

    private var launchedDialer = false
    var cachedContacts = ArrayList<Contact>()
    private var recentsHelper: RecentsHelper? = null

    // Compose Reactive State
    private var selectedTabState by mutableIntStateOf(0)
    private var isSearchActiveState by mutableStateOf(false)
    private var searchQueryState by mutableStateOf("")
    private var recentsState by mutableStateOf<List<CallLogItem>>(emptyList())
    private var contactsState by mutableStateOf<List<Contact>>(emptyList())
    private var favoritesState by mutableStateOf<List<Contact>>(emptyList())
    private var speedDialState by mutableStateOf<List<SpeedDial>>(emptyList())
    private var dialedNumberState by mutableStateOf("")
    private var showImeiDialogState by mutableStateOf(false)
    private var hasCallLogPermissionState by mutableStateOf(false)
    private var hasContactsPermissionState by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        recentsHelper = RecentsHelper(this)
        appLaunched(BuildConfig.APPLICATION_ID)
        EventBus.getDefault().register(this)

        launchedDialer = savedInstanceState?.getBoolean(OPEN_DIAL_PAD_AT_LAUNCH) ?: false
        selectedTabState = if (config.openDialPadAtLaunch && !launchedDialer) {
            launchedDialer = true
            2
        } else {
            getDefaultTab()
        }

        if (isDefaultDialer()) {
            checkPermissions()
            handleFullScreenNotificationsPermission { granted ->
                if (!granted) {
                    toast(org.fossify.commons.R.string.notifications_disabled)
                }
            }
        } else {
            launchSetDefaultDialerIntent()
        }

        if (isQPlus() && (config.blockUnknownNumbers || config.blockHiddenNumbers)) {
            setDefaultCallerIdApp()
        }

        Contact.sorting = config.sorting
        handleDialIntent(intent)

        setContent {
            PhoneTheme(activity = this) {
                MainScreen(
                    initialTab = selectedTabState,
                    searchQuery = searchQueryState,
                    onSearchQueryChange = { query ->
                        searchQueryState = query
                    },
                    isSearchActive = isSearchActiveState,
                    onSearchActiveChange = { active ->
                        isSearchActiveState = active
                    },
                    // Recents
                    recents = recentsState,
                    hasCallLogPermission = hasCallLogPermissionState,
                    onCallClick = { recentCall ->
                        startCallWithConfirmationCheck(recentCall.phoneNumber, recentCall.name)
                    },
                    onRecentProfileClick = { recentCall ->
                        val contact = findContactByCall(recentCall)
                        if (contact != null) {
                            startContactDetailsIntent(contact)
                        } else {
                            startAddContactIntent(recentCall.phoneNumber)
                        }
                    },
                    onDeleteRecentCall = { recentCall ->
                        recentsState = recentsState.filter { (it as? RecentCall)?.id != recentCall.id }
                        recentsHelper?.removeRecentCalls(listOf(recentCall.id)) {}
                    },
                    onSendSms = { number ->
                        launchSendSMSIntent(number)
                    },
                    onRequestCallLogPermission = {
                        requestCallLogPermission()
                    },
                    onClearCallHistory = {
                        recentsHelper?.removeAllRecentCalls(this) {
                            runOnUiThread {
                                refreshRecents()
                            }
                        }
                    },
                    // Contacts
                    contacts = contactsState,
                    favorites = favoritesState,
                    hasContactsPermission = hasContactsPermissionState,
                    onContactClick = { contact ->
                        handleGenericContactClick(contact)
                    },
                    onContactProfileClick = { contact ->
                        startContactDetailsIntent(contact)
                    },
                    onCreateContactClick = {
                        launchCreateNewContactIntent()
                    },
                    onRequestContactsPermission = {
                        requestContactsPermission()
                    },
                    onSortClick = {
                        showSortingDialog()
                    },
                    onFilterClick = {
                        showFilterDialog()
                    },
                    // Keypad
                    dialedNumber = dialedNumberState,
                    onAppendChar = { char ->
                        appendDialpadChar(char)
                    },
                    onDeleteChar = {
                        if (dialedNumberState.isNotEmpty()) {
                            dialedNumberState = dialedNumberState.dropLast(1)
                        }
                    },
                    onClearAllDialed = {
                        dialedNumberState = ""
                    },
                    onSetDialedNumber = { number ->
                        setDialedNumber(number)
                    },
                    speedDialValues = speedDialState,
                    onDialpadCallClick = { number, name ->
                        handleDialpadCall(number, name)
                    },
                    onDialpadCallWithSimSelector = { number ->
                        handleDialpadCallWithSimSelector(number)
                    },
                    onDialpadAddContactClick = { number ->
                        startAddContactIntent(number)
                    },
                    showImeiDialog = showImeiDialogState,
                    onDismissImeiDialog = {
                        showImeiDialogState = false
                    },
                    // Global Navigation
                    onTabSelected = { tabIndex ->
                        selectedTabState = tabIndex
                        config.lastUsedViewPagerPage = tabIndex
                        if (tabIndex == 0) {
                            clearMissedCalls()
                        }
                    },
                    onSettingsClick = {
                        launchSettings()
                    },
                    onAboutClick = {
                        launchAbout()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshAll()
        checkShortcuts()
    }

    override fun onPause() {
        super.onPause()
        config.lastUsedViewPagerPage = selectedTabState
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDialIntent(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == REQUEST_CODE_SET_DEFAULT_DIALER) {
            checkPermissions()
        } else if (requestCode == REQUEST_CODE_SET_DEFAULT_CALLER_ID && resultCode != Activity.RESULT_OK) {
            toast(R.string.must_make_default_caller_id_app, length = Toast.LENGTH_LONG)
            baseConfig.blockUnknownNumbers = false
            baseConfig.blockHiddenNumbers = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(OPEN_DIAL_PAD_AT_LAUNCH, launchedDialer)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        refreshAll()
    }

    override fun onBackPressedCompat(): Boolean {
        if (isSearchActiveState) {
            isSearchActiveState = false
            searchQueryState = ""
            return true
        }
        val defTab = getDefaultTab()
        if (selectedTabState != defTab) {
            selectedTabState = defTab
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun handleDialIntent(intent: Intent) {
        if (intent.action == Intent.ACTION_DIAL || (intent.action == Intent.ACTION_VIEW && intent.data?.scheme == "tel")) {
            selectedTabState = 2
            intent.data?.schemeSpecificPart?.let { number ->
                setDialedNumber(number)
            }
        } else if (intent.action == Intent.ACTION_VIEW) {
            selectedTabState = 0
            clearMissedCalls()
        }
    }

    private fun checkPermissions() {
        handlePermission(PERMISSION_READ_CONTACTS) { readGranted ->
            hasContactsPermissionState = readGranted
            if (readGranted) {
                handlePermission(PERMISSION_WRITE_CONTACTS) {
                    DebugContactsHelper.insertSampleContactsIfEmpty(this) {
                        runOnUiThread {
                            refreshContacts()
                        }
                    }
                }
            }
        }

        handlePermission(PERMISSION_READ_CALL_LOG) { logGranted ->
            hasCallLogPermissionState = logGranted
            if (logGranted) {
                refreshRecents()
            }
        }
    }

    private fun requestContactsPermission() {
        handlePermission(PERMISSION_READ_CONTACTS) { granted ->
            hasContactsPermissionState = granted
            if (granted) {
                refreshContacts()
            }
        }
    }

    private fun requestCallLogPermission() {
        handlePermission(PERMISSION_READ_CALL_LOG) { granted ->
            hasCallLogPermissionState = granted
            if (granted) {
                refreshRecents()
            }
        }
    }

    fun refreshAll() {
        speedDialState = config.getSpeedDialValues()
        hasContactsPermissionState = hasPermission(PERMISSION_READ_CONTACTS)
        hasCallLogPermissionState = hasPermission(PERMISSION_READ_CALL_LOG)
        refreshContacts()
        refreshRecents()
    }

    fun refreshFragments() {
        refreshAll()
    }

    private fun refreshContacts() {
        if (!hasPermission(PERMISSION_READ_CONTACTS)) {
            contactsState = emptyList()
            favoritesState = emptyList()
            return
        }

        val privateCursor = getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)
        ContactsHelper(this).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
            val allContacts = ArrayList(contacts)

            if (SMT_PRIVATE !in baseConfig.ignoredContactSources) {
                val privateContacts = MyContactsContentProvider.getContacts(this, privateCursor)
                if (privateContacts.isNotEmpty()) {
                    allContacts.addAll(privateContacts)
                    allContacts.sort()
                }
            }

            if (debugMode && allContacts.isEmpty()) {
                DebugContactsHelper.insertSampleContactsIfEmpty(this) {
                    ContactsHelper(this).getContacts(showOnlyContactsWithNumbers = true) { reloaded ->
                        gotContacts(reloaded)
                    }
                }
            } else {
                gotContacts(allContacts)
            }
        }
    }

    private fun gotContacts(contacts: List<Contact>) {
        try {
            cachedContacts.clear()
            cachedContacts.addAll(contacts)
        } catch (_: Exception) {}

        val favorites = contacts.filter { it.starred == 1 }
        runOnUiThread {
            contactsState = contacts
            favoritesState = favorites
        }
    }

    private fun refreshRecents() {
        if (!hasPermission(PERMISSION_READ_CALL_LOG)) {
            recentsState = emptyList()
            return
        }

        recentsHelper?.getRecentCalls(queryLimit = -1) { calls ->
            prepareCallLog(calls) { prepared ->
                runOnUiThread {
                    recentsState = if (debugMode && prepared.isEmpty()) {
                        DebugContactsHelper.getSampleCallLogs()
                    } else {
                        prepared
                    }
                }
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

    private fun findContactByCall(recentCall: RecentCall): Contact? {
        return cachedContacts.find {
            it.name == recentCall.name && it.doesHavePhoneNumber(recentCall.phoneNumber)
        }
    }

    private fun appendDialpadChar(char: Char) {
        val candidate = dialedNumberState + char
        if (candidate == "*#06#") {
            dialedNumberState = ""
            showImeiDialogState = true
            handleSpecialCode("06")
        } else if (candidate.length > 8 && candidate.startsWith("*#*#") && candidate.endsWith("#*#*")) {
            val secretCode = candidate.substring(4, candidate.length - 4)
            dialedNumberState = ""
            handleSpecialCode(secretCode)
        } else {
            dialedNumberState = candidate
        }
    }

    fun setDialedNumber(number: String) {
        if (number == "*#06#") {
            dialedNumberState = ""
            showImeiDialogState = true
            handleSpecialCode("06")
        } else if (number.length > 8 && number.startsWith("*#*#") && number.endsWith("#*#*")) {
            val secretCode = number.substring(4, number.length - 4)
            dialedNumberState = ""
            handleSpecialCode(secretCode)
        } else {
            dialedNumberState = number
        }
    }

    private fun handleDialpadCall(number: String, name: String? = null) {
        if (number == "*#06#") {
            dialedNumberState = ""
            showImeiDialogState = true
            handleSpecialCode("06")
            return
        }
        if (number.isNotEmpty()) {
            startCallWithConfirmationCheck(number, name ?: number)
        }
    }

    private fun handleDialpadCallWithSimSelector(number: String): Boolean {
        return if (areMultipleSIMsAvailable() && number.isNotEmpty()) {
            startCallWithConfirmationCheck(
                recipient = number,
                name = number,
                forceSimSelector = true
            )
            true
        } else {
            false
        }
    }

    private fun handleSpecialCode(code: String) {
        if (isOreoPlus()) {
            if (isDefaultDialer()) {
                try {
                    getSystemService(TelephonyManager::class.java)?.sendDialerSpecialCode(code)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                launchDefaultDialer()
            }
        } else {
            try {
                val intent = Intent(Telephony.Sms.Intents.SECRET_CODE_ACTION, "android_secret_code://$code".toUri())
                sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun clearCallHistory() {
        val confirmationText = "${getString(R.string.clear_history_confirmation)}\n\n${getString(R.string.cannot_be_undone)}"
        ConfirmationDialog(this, confirmationText) {
            recentsHelper?.removeAllRecentCalls(this) {
                runOnUiThread {
                    refreshRecents()
                }
            }
        }
    }

    private fun showSortingDialog() {
        ChangeSortingDialog(this, false) {
            refreshContacts()
        }
    }

    private fun showFilterDialog() {
        FilterContactSourcesDialog(this) {
            refreshContacts()
            refreshRecents()
        }
    }

    private fun launchSettings() {
        startActivity(Intent(applicationContext, SettingsActivity::class.java))
    }

    private fun launchAbout() {
        val licenses = LICENSE_GLIDE or LICENSE_INDICATOR_FAST_SCROLL or LICENSE_AUTOFITTEXTVIEW

        val faqItems = arrayListOf(
            FAQItem(R.string.faq_1_title, R.string.faq_1_text),
            FAQItem(R.string.faq_2_title, R.string.faq_2_text),
            FAQItem(R.string.faq_3_title, R.string.faq_3_text),
            FAQItem(R.string.faq_9_title_commons, R.string.faq_9_text_commons)
        )

        if (resources.getBoolean(R.bool.is_google_play_build)) {
            faqItems.add(FAQItem(R.string.faq_2_title_commons, R.string.faq_2_text_commons))
            faqItems.add(FAQItem(R.string.faq_6_title_commons, R.string.faq_6_text_commons))
        }

        startAboutActivity(R.string.app_name, licenses, BuildConfig.VERSION_NAME, faqItems, true)
    }

    private fun getDefaultTab(): Int {
        val defaultTab = config.defaultTab
        if (defaultTab == TAB_LAST_USED) {
            return config.lastUsedViewPagerPage.coerceIn(0, 2)
        }
        val activeTabs = tabsList.filter { it and config.showTabs != 0 }
        val index = activeTabs.indexOf(defaultTab)
        return if (index != -1) index else 0
    }

    @SuppressLint("NewApi")
    private fun checkShortcuts() {
        val appIconColor = config.appIconColor
        if (isNougatMR1Plus() && config.lastHandledShortcutColor != appIconColor) {
            val newEvent = getString(R.string.dialpad)
            val drawable = resources.getDrawable(R.drawable.shortcut_dialpad)
            (drawable as? LayerDrawable)?.findDrawableByLayerId(R.id.shortcut_dialpad_background)?.applyColorFilter(appIconColor)
            val bmp = drawable?.convertToBitmap() ?: return

            val intent = Intent(this, DialpadActivity::class.java).apply {
                action = Intent.ACTION_VIEW
            }
            val shortcut = ShortcutInfo.Builder(this, "launch_dialpad")
                .setShortLabel(newEvent)
                .setLongLabel(newEvent)
                .setIcon(Icon.createWithBitmap(bmp))
                .setIntent(intent)
                .build()

            try {
                shortcutManager.dynamicShortcuts = listOf(shortcut)
                config.lastHandledShortcutColor = appIconColor
            } catch (_: Exception) {}
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun refreshCallLog(event: Events.RefreshCallLog) {
        refreshRecents()
    }
}
