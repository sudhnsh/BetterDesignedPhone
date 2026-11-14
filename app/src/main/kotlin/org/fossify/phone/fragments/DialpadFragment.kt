package org.fossify.phone.fragments

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.util.AttributeSet
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import org.fossify.commons.extensions.getMyContactsCursor
import org.fossify.commons.extensions.isDefaultDialer
import org.fossify.commons.helpers.ContactsHelper
import org.fossify.commons.helpers.MyContactsContentProvider
import org.fossify.commons.helpers.isOreoPlus
import org.fossify.commons.models.contacts.Contact
import org.fossify.phone.R
import org.fossify.phone.activities.MainActivity
import org.fossify.phone.activities.SimpleActivity
import org.fossify.phone.compose.screens.DialpadScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.extensions.areMultipleSIMsAvailable
import org.fossify.phone.extensions.config
import org.fossify.phone.extensions.startAddContactIntent
import org.fossify.phone.extensions.startCallWithConfirmationCheck
import org.fossify.phone.extensions.startContactDetailsIntent
import org.fossify.phone.interfaces.RefreshItemsListener
import org.fossify.phone.models.SpeedDial

class DialpadFragment(context: Context, attributeSet: AttributeSet) :
    MyViewPagerFragment<MyViewPagerFragment.ComposeInnerBinding>(context, attributeSet),
    RefreshItemsListener {

    private var contactsState by mutableStateOf<List<Contact>>(emptyList())
    private var speedDialState by mutableStateOf<List<SpeedDial>>(emptyList())
    private var dialedNumberState by mutableStateOf("")
    private var showImeiDialogState by mutableStateOf(false)
    private var composeView: ComposeView? = null
    private var privateCursor: Cursor? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        innerBinding = ComposeInnerBinding()
        composeView = findViewById(R.id.compose_view)
    }

    override fun setupFragment() {
        val act = activity ?: return
        speedDialState = act.config.getSpeedDialValues()
        privateCursor = context.getMyContactsCursor(favoritesOnly = false, withPhoneNumbersOnly = true)

        composeView?.setContent {
            PhoneTheme(activity = activity) {
                DialpadScreen(
                    dialedNumber = dialedNumberState,
                    showImeiDialog = showImeiDialogState,
                    onDismissImeiDialog = {
                        showImeiDialogState = false
                    },
                    onAppendChar = { char ->
                        appendChar(char)
                    },
                    onDeleteChar = {
                        if (dialedNumberState.isNotEmpty()) {
                            dialedNumberState = dialedNumberState.dropLast(1)
                        }
                    },
                    onClearAll = {
                        dialedNumberState = ""
                    },
                    onSetNumber = { number ->
                        setNumber(number)
                    },
                    allContacts = contactsState,
                    speedDialValues = speedDialState,
                    onCallClick = { number, name ->
                        handleCallClick(number, name)
                    },
                    onCallWithSimSelector = { number ->
                        initCallWithSimSelector(number)
                    },
                    onContactDetailsClick = { contact ->
                        activity?.startContactDetailsIntent(contact)
                    },
                    onAddContactClick = {
                        addNumberToContact()
                    }
                )
            }
        }

        refreshItems()
    }

    private fun appendChar(char: Char) {
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

    private fun handleCallClick(number: String, name: String? = null) {
        if (number == "*#06#") {
            dialedNumberState = ""
            showImeiDialogState = true
            handleSpecialCode("06")
            return
        }
        initCall(number, name)
    }

    private fun handleSpecialCode(code: String) {
        val act = activity ?: return
        if (isOreoPlus()) {
            if (act.isDefaultDialer()) {
                try {
                    act.getSystemService(TelephonyManager::class.java)?.sendDialerSpecialCode(code)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
                act.launchDefaultDialer()
            }
        } else {
            try {
                val intent = Intent(Telephony.Sms.Intents.SECRET_CODE_ACTION, "android_secret_code://$code".toUri())
                act.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun refreshItems(invalidate: Boolean, callback: (() -> Unit)?) {
        val act = activity ?: return
        speedDialState = act.config.getSpeedDialValues()
        val cached = (act as? MainActivity)?.cachedContacts
        if (!invalidate && !cached.isNullOrEmpty()) {
            gotContacts(cached)
            callback?.invoke()
        } else {
            ContactsHelper(context).getContacts(showOnlyContactsWithNumbers = true) { contacts ->
                gotContacts(contacts)
                callback?.invoke()
            }
        }
    }

    private fun gotContacts(newContacts: List<Contact>) {
        val allContacts = ArrayList(newContacts)
        val privateContacts = MyContactsContentProvider.getContacts(context, privateCursor)
        if (privateContacts.isNotEmpty()) {
            allContacts.addAll(privateContacts)
            allContacts.sort()
        }
        post {
            contactsState = allContacts
        }
    }

    override fun setupColors(textColor: Int, primaryColor: Int, properPrimaryColor: Int) {
        // Handled reactively by Compose PhoneTheme
    }

    override fun onSearchClosed() {}

    override fun onSearchQueryChanged(text: String) {}

    fun setNumber(number: String) {
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

    fun addNumberToContact() {
        val act = activity ?: return
        act.startAddContactIntent(dialedNumberState)
    }

    private fun initCall(number: String, name: String? = null) {
        val act = activity ?: return
        if (number.isNotEmpty()) {
            act.startCallWithConfirmationCheck(number, name ?: number)
        }
    }

    private fun initCallWithSimSelector(number: String): Boolean {
        val act = activity ?: return false
        return if (act.areMultipleSIMsAvailable() && number.isNotEmpty()) {
            act.startCallWithConfirmationCheck(
                recipient = number,
                name = number,
                forceSimSelector = true
            )
            true
        } else {
            false
        }
    }
}
