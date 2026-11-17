package org.fossify.phone.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.graphics.ColorUtils
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.fossify.commons.activities.ManageBlockedNumbersActivity
import org.fossify.commons.dialogs.ChangeDateTimeFormatDialog
import org.fossify.commons.dialogs.RadioGroupDialog
import org.fossify.commons.extensions.baseConfig
import org.fossify.commons.extensions.getFontSizeText
import org.fossify.commons.extensions.showErrorToast
import org.fossify.commons.extensions.toast
import org.fossify.commons.helpers.FONT_SIZE_EXTRA_LARGE
import org.fossify.commons.helpers.FONT_SIZE_LARGE
import org.fossify.commons.helpers.FONT_SIZE_MEDIUM
import org.fossify.commons.helpers.FONT_SIZE_SMALL
import org.fossify.commons.helpers.ON_CLICK_CALL_CONTACT
import org.fossify.commons.helpers.ON_CLICK_VIEW_CONTACT
import org.fossify.commons.helpers.TAB_CALL_HISTORY
import org.fossify.commons.helpers.TAB_CONTACTS
import org.fossify.commons.helpers.TAB_LAST_USED
import org.fossify.commons.helpers.isNougatPlus
import org.fossify.commons.helpers.isQPlus
import org.fossify.commons.helpers.isTiramisuPlus
import org.fossify.commons.models.RadioItem
import org.fossify.phone.R
import org.fossify.phone.compose.screens.SettingsScreen
import org.fossify.phone.compose.theme.PhoneTheme
import org.fossify.phone.dialogs.ExportCallHistoryDialog
import org.fossify.phone.dialogs.ManageVisibleTabsDialog
import org.fossify.phone.dialogs.showConfigureImeiDialog
import org.fossify.phone.extensions.canLaunchAccountsConfiguration
import org.fossify.phone.extensions.config
import org.fossify.phone.extensions.launchAccountsConfiguration
import org.fossify.phone.helpers.AppIconHelper
import org.fossify.phone.helpers.RecentsHelper
import org.fossify.phone.helpers.TAB_DIALPAD
import org.fossify.phone.models.RecentCall
import java.util.Locale
import kotlin.system.exitProcess

class SettingsActivity : SimpleActivity() {
    companion object {
        private const val THEME_SYSTEM = 0
        private const val THEME_LIGHT = 1
        private const val THEME_DARK = 2

        private const val ICON_DARK_ID = 0
        private const val ICON_LIGHT_ID = 1

        private const val CALL_HISTORY_FILE_TYPE = "application/json"
        private val IMPORT_CALL_HISTORY_FILE_TYPES = buildList {
            add("application/json")
            if (!isQPlus()) {
                add("application/octet-stream")
            }
        }
    }

    private var themeTextState by mutableStateOf("")
    private var oledModeState by mutableStateOf(false)
    private var isOledScreenState by mutableStateOf(false)
    private var appIconTextState by mutableStateOf("")
    private var useEnglishState by mutableStateOf(false)
    private var imeiTextState by mutableStateOf("")
    private var fontSizeTextState by mutableStateOf("")
    private var defaultTabTextState by mutableStateOf("")
    private var onContactClickTextState by mutableStateOf("")
    private var openDialpadAtLaunchState by mutableStateOf(false)
    private var groupSubsequentCallsState by mutableStateOf(false)
    private var showCallConfirmationState by mutableStateOf(false)
    private var startNameWithSurnameState by mutableStateOf(false)
    private var formatPhoneNumbersState by mutableStateOf(false)
    private var disableProximitySensorState by mutableStateOf(false)
    private var disableSwipeToAnswerState by mutableStateOf(false)
    private var alwaysShowFullscreenState by mutableStateOf(false)
    private var dialpadVibrationState by mutableStateOf(false)
    private var hideDialpadNumbersState by mutableStateOf(false)
    private var dialpadBeepsState by mutableStateOf(false)

    private val getContent =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                toast(R.string.importing)
                importCallHistory(uri)
            }
        }

    private val saveDocument = registerForActivityResult(ActivityResultContracts.CreateDocument(CALL_HISTORY_FILE_TYPE)) { uri ->
        if (uri != null) {
            toast(R.string.exporting)
            RecentsHelper(this).getRecentCalls(queryLimit = Int.MAX_VALUE) { recents ->
                exportCallHistory(recents, uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        refreshStates()

        setContent {
            PhoneTheme(activity = this) {
                SettingsScreen(
                    onBackClick = { finish() },
                    canLaunchCallingAccounts = canLaunchAccountsConfiguration(),
                    onCallingAccountsClick = { launchAccountsConfiguration() },

                    // Theme & Appearance
                    themeText = themeTextState,
                    onThemeClick = { showThemeDialog() },
                    oledModeEnabled = oledModeState,
                    onOledModeChange = { enabled ->
                        config.oledModeEnabled = enabled
                        oledModeState = enabled
                    },
                    isOledScreen = isOledScreenState,
                    onScreenTypeClick = { showScreenTypeDialog() },
                    appIconText = appIconTextState,
                    onAppIconClick = { showAppIconDialog() },

                    // General
                    showUseEnglish = (config.wasUseEnglishToggled || Locale.getDefault().language != "en") && !isTiramisuPlus(),
                    useEnglish = useEnglishState,
                    onUseEnglishChange = { enabled ->
                        config.useEnglish = enabled
                        useEnglishState = enabled
                        exitProcess(0)
                    },
                    showLanguage = isTiramisuPlus(),
                    languageText = Locale.getDefault().displayLanguage,
                    onLanguageClick = { launchChangeAppLanguageIntent() },
                    showBlockedNumbers = isNougatPlus(),
                    onBlockedNumbersClick = {
                        startActivity(Intent(this, ManageBlockedNumbersActivity::class.java))
                    },
                    onSpeedDialClick = {
                        startActivity(Intent(this, ManageSpeedDialActivity::class.java))
                    },
                    imeiText = imeiTextState,
                    onImeiClick = {
                        showConfigureImeiDialog(this) {
                            imeiTextState = getImeiSettingsValue()
                        }
                    },
                    onChangeDateTimeFormatClick = {
                        ChangeDateTimeFormatDialog(this) {}
                    },
                    fontSizeText = fontSizeTextState,
                    onFontSizeClick = { showFontSizeDialog() },
                    onManageTabsClick = { ManageVisibleTabsDialog(this) },
                    defaultTabText = defaultTabTextState,
                    onDefaultTabClick = { showDefaultTabDialog() },
                    onContactClickText = onContactClickTextState,
                    onContactClickAction = { showOnContactClickDialog() },

                    // Main screen
                    openDialpadAtLaunch = openDialpadAtLaunchState,
                    onOpenDialpadAtLaunchChange = { enabled ->
                        config.openDialPadAtLaunch = enabled
                        openDialpadAtLaunchState = enabled
                    },

                    // Calls
                    groupSubsequentCalls = groupSubsequentCallsState,
                    onGroupSubsequentCallsChange = { enabled ->
                        config.groupSubsequentCalls = enabled
                        groupSubsequentCallsState = enabled
                    },
                    showCallConfirmation = showCallConfirmationState,
                    onShowCallConfirmationChange = { enabled ->
                        config.showCallConfirmation = enabled
                        showCallConfirmationState = enabled
                    },
                    startNameWithSurname = startNameWithSurnameState,
                    onStartNameWithSurnameChange = { enabled ->
                        config.startNameWithSurname = enabled
                        startNameWithSurnameState = enabled
                    },
                    formatPhoneNumbers = formatPhoneNumbersState,
                    onFormatPhoneNumbersChange = { enabled ->
                        config.formatPhoneNumbers = enabled
                        formatPhoneNumbersState = enabled
                    },
                    disableProximitySensor = disableProximitySensorState,
                    onDisableProximitySensorChange = { enabled ->
                        config.disableProximitySensor = enabled
                        disableProximitySensorState = enabled
                    },
                    disableSwipeToAnswer = disableSwipeToAnswerState,
                    onDisableSwipeToAnswerChange = { enabled ->
                        config.disableSwipeToAnswer = enabled
                        disableSwipeToAnswerState = enabled
                    },
                    alwaysShowFullscreen = alwaysShowFullscreenState,
                    onAlwaysShowFullscreenChange = { enabled ->
                        config.alwaysShowFullscreen = enabled
                        alwaysShowFullscreenState = enabled
                    },

                    // Dialpad
                    dialpadVibration = dialpadVibrationState,
                    onDialpadVibrationChange = { enabled ->
                        config.dialpadVibration = enabled
                        dialpadVibrationState = enabled
                    },
                    hideDialpadNumbers = hideDialpadNumbersState,
                    onHideDialpadNumbersChange = { enabled ->
                        config.hideDialpadNumbers = enabled
                        hideDialpadNumbersState = enabled
                    },
                    dialpadBeeps = dialpadBeepsState,
                    onDialpadBeepsChange = { enabled ->
                        config.dialpadBeeps = enabled
                        dialpadBeepsState = enabled
                    },

                    // Migrating
                    onExportCallsClick = {
                        ExportCallHistoryDialog(this) { filename ->
                            saveDocument.launch("$filename.json")
                        }
                    },
                    onImportCallsClick = {
                        getContent.launch(IMPORT_CALL_HISTORY_FILE_TYPES.toTypedArray())
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStates()
    }

    private fun refreshStates() {
        themeTextState = getThemeText()
        oledModeState = config.oledModeEnabled
        isOledScreenState = config.isOledScreen
        appIconTextState = getAppIconText()
        useEnglishState = config.useEnglish
        imeiTextState = getImeiSettingsValue()
        fontSizeTextState = getFontSizeText()
        defaultTabTextState = getDefaultTabText()
        onContactClickTextState = getOnContactClickText()
        openDialpadAtLaunchState = config.openDialPadAtLaunch
        groupSubsequentCallsState = config.groupSubsequentCalls
        showCallConfirmationState = config.showCallConfirmation
        startNameWithSurnameState = config.startNameWithSurname
        formatPhoneNumbersState = config.formatPhoneNumbers
        disableProximitySensorState = config.disableProximitySensor
        disableSwipeToAnswerState = config.disableSwipeToAnswer
        alwaysShowFullscreenState = config.alwaysShowFullscreen
        dialpadVibrationState = config.dialpadVibration
        hideDialpadNumbersState = config.hideDialpadNumbers
        dialpadBeepsState = config.dialpadBeeps
    }

    private fun showThemeDialog() {
        val items = arrayListOf(
            RadioItem(THEME_SYSTEM, getString(R.string.theme_system)),
            RadioItem(THEME_LIGHT, getString(R.string.theme_light)),
            RadioItem(THEME_DARK, getString(R.string.theme_dark))
        )

        RadioGroupDialog(this, items, getCurrentThemeId()) { selected ->
            when (selected as Int) {
                THEME_SYSTEM -> {
                    baseConfig.isSystemThemeEnabled = true
                }
                THEME_LIGHT -> {
                    baseConfig.isSystemThemeEnabled = false
                    baseConfig.backgroundColor = 0xFFFFFFFF.toInt()
                    baseConfig.textColor = 0xFF1C1C1E.toInt()
                }
                THEME_DARK -> {
                    baseConfig.isSystemThemeEnabled = false
                    baseConfig.backgroundColor = 0xFF111111.toInt()
                    baseConfig.textColor = 0xFFFFFFFF.toInt()
                }
            }
            themeTextState = getThemeText()
            recreate()
        }
    }

    private fun showScreenTypeDialog() {
        val items = arrayListOf(
            RadioItem(1, "OLED / AMOLED"),
            RadioItem(0, "LCD")
        )
        val currentId = if (config.isOledScreen) 1 else 0
        RadioGroupDialog(this, items, currentId) { selected ->
            val isOled = (selected == 1)
            config.isOledScreen = isOled
            config.hasPromptedOledConfirmation = true
            isOledScreenState = isOled
            if (isOled) {
                config.oledModeEnabled = true
                oledModeState = true
            }
        }
    }

    private fun showAppIconDialog() {
        val items = arrayListOf(
            RadioItem(ICON_DARK_ID, getString(R.string.app_icon_dark)),
            RadioItem(ICON_LIGHT_ID, getString(R.string.app_icon_light))
        )

        RadioGroupDialog(this, items, getCurrentIconId()) { selected ->
            val icon = if ((selected as Int) == ICON_LIGHT_ID) AppIconHelper.ICON_LIGHT else AppIconHelper.ICON_DARK
            AppIconHelper.setAppIcon(this, icon)
            appIconTextState = getAppIconText()
        }
    }

    private fun showFontSizeDialog() {
        val items = arrayListOf(
            RadioItem(FONT_SIZE_SMALL, getString(R.string.small)),
            RadioItem(FONT_SIZE_MEDIUM, getString(R.string.medium)),
            RadioItem(FONT_SIZE_LARGE, getString(R.string.large)),
            RadioItem(FONT_SIZE_EXTRA_LARGE, getString(R.string.extra_large))
        )

        RadioGroupDialog(this, items, config.fontSize) { selected ->
            config.fontSize = selected as Int
            fontSizeTextState = getFontSizeText()
        }
    }

    private fun showDefaultTabDialog() {
        val items = arrayListOf(
            RadioItem(TAB_CALL_HISTORY, getString(R.string.tab_call)),
            RadioItem(TAB_CONTACTS, getString(R.string.contacts_tab)),
            RadioItem(TAB_DIALPAD, getString(R.string.tab_keypad)),
            RadioItem(TAB_LAST_USED, getString(R.string.last_used_tab))
        )

        RadioGroupDialog(this, items, config.defaultTab) { selected ->
            config.defaultTab = selected as Int
            defaultTabTextState = getDefaultTabText()
        }
    }

    private fun showOnContactClickDialog() {
        val items = arrayListOf(
            RadioItem(ON_CLICK_CALL_CONTACT, getString(org.fossify.commons.R.string.call_contact)),
            RadioItem(ON_CLICK_VIEW_CONTACT, getString(org.fossify.commons.R.string.view_contact))
        )

        RadioGroupDialog(this, items, config.onContactClick) { selected ->
            config.onContactClick = selected as Int
            onContactClickTextState = getOnContactClickText()
        }
    }

    private fun getCurrentThemeId(): Int {
        return if (baseConfig.isSystemThemeEnabled) {
            THEME_SYSTEM
        } else if (ColorUtils.calculateLuminance(baseConfig.backgroundColor) > 0.5) {
            THEME_LIGHT
        } else {
            THEME_DARK
        }
    }

    private fun getThemeText(): String {
        return when (getCurrentThemeId()) {
            THEME_LIGHT -> getString(R.string.theme_light)
            THEME_DARK -> getString(R.string.theme_dark)
            else -> getString(R.string.theme_system)
        }
    }

    private fun getCurrentIconId(): Int {
        return if (AppIconHelper.getCurrentIcon(this) == AppIconHelper.ICON_LIGHT) ICON_LIGHT_ID else ICON_DARK_ID
    }

    private fun getAppIconText(): String {
        return if (getCurrentIconId() == ICON_LIGHT_ID) {
            getString(R.string.app_icon_light)
        } else {
            getString(R.string.app_icon_dark)
        }
    }

    private fun getDefaultTabText() = getString(
        when (baseConfig.defaultTab) {
            TAB_CALL_HISTORY -> R.string.tab_call
            TAB_CONTACTS -> R.string.contacts_tab
            TAB_DIALPAD -> R.string.tab_keypad
            else -> R.string.last_used_tab
        }
    )

    private fun getOnContactClickText() = getString(
        when (config.onContactClick) {
            ON_CLICK_CALL_CONTACT -> org.fossify.commons.R.string.call_contact
            else -> org.fossify.commons.R.string.view_contact
        }
    )

    private fun getImeiSettingsValue(): String {
        val imei1 = config.customImei1
        val imei2 = config.customImei2
        return when {
            imei1.isNotEmpty() && imei2.isNotEmpty() -> "IMEI 1: $imei1 | IMEI 2: $imei2"
            imei1.isNotEmpty() -> "IMEI 1: $imei1"
            else -> "Configure IMEI numbers & barcodes"
        }
    }

    private fun importCallHistory(uri: Uri) {
        try {
            val jsonString = contentResolver.openInputStream(uri)!!.use { inputStream ->
                inputStream.bufferedReader().readText()
            }

            val objects = Json.decodeFromString<List<RecentCall>>(jsonString)

            if (objects.isEmpty()) {
                toast(R.string.no_entries_for_importing)
                return
            }

            RecentsHelper(this).restoreRecentCalls(this, objects) {
                toast(R.string.importing_successful)
            }
        } catch (_: SerializationException) {
            toast(R.string.invalid_file_format)
        } catch (_: IllegalArgumentException) {
            toast(R.string.invalid_file_format)
        } catch (e: Exception) {
            showErrorToast(e)
        }
    }

    private fun exportCallHistory(recents: List<RecentCall>, uri: Uri) {
        if (recents.isEmpty()) {
            toast(R.string.no_entries_for_exporting)
        } else {
            try {
                val outputStream = contentResolver.openOutputStream(uri)!!

                val jsonString = Json.encodeToString(recents)
                outputStream.use {
                    it.write(jsonString.toByteArray())
                }
                toast(R.string.exporting_successful)
            } catch (e: Exception) {
                showErrorToast(e)
            }
        }
    }
}
