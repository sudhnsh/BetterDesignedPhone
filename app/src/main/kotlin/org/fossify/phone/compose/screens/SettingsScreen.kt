package org.fossify.phone.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.fossify.phone.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    // Top Bar
    onBackClick: () -> Unit,
    canLaunchCallingAccounts: Boolean,
    onCallingAccountsClick: () -> Unit,

    // Theme & Appearance
    themeText: String,
    onThemeClick: () -> Unit,
    oledModeEnabled: Boolean,
    onOledModeChange: (Boolean) -> Unit,
    isOledScreen: Boolean,
    onScreenTypeClick: () -> Unit,
    appIconText: String,
    onAppIconClick: () -> Unit,

    // General
    showUseEnglish: Boolean,
    useEnglish: Boolean,
    onUseEnglishChange: (Boolean) -> Unit,
    showLanguage: Boolean,
    languageText: String,
    onLanguageClick: () -> Unit,
    showBlockedNumbers: Boolean,
    onBlockedNumbersClick: () -> Unit,
    onSpeedDialClick: () -> Unit,
    imeiText: String,
    onImeiClick: () -> Unit,
    onChangeDateTimeFormatClick: () -> Unit,
    fontSizeText: String,
    onFontSizeClick: () -> Unit,
    onManageTabsClick: () -> Unit,
    defaultTabText: String,
    onDefaultTabClick: () -> Unit,
    onContactClickText: String,
    onContactClickAction: () -> Unit,

    // Main screen
    openDialpadAtLaunch: Boolean,
    onOpenDialpadAtLaunchChange: (Boolean) -> Unit,

    // Calls
    groupSubsequentCalls: Boolean,
    onGroupSubsequentCallsChange: (Boolean) -> Unit,
    showCallConfirmation: Boolean,
    onShowCallConfirmationChange: (Boolean) -> Unit,
    startNameWithSurname: Boolean,
    onStartNameWithSurnameChange: (Boolean) -> Unit,
    formatPhoneNumbers: Boolean,
    onFormatPhoneNumbersChange: (Boolean) -> Unit,
    disableProximitySensor: Boolean,
    onDisableProximitySensorChange: (Boolean) -> Unit,
    disableSwipeToAnswer: Boolean,
    onDisableSwipeToAnswerChange: (Boolean) -> Unit,
    alwaysShowFullscreen: Boolean,
    onAlwaysShowFullscreenChange: (Boolean) -> Unit,

    // Dialpad
    dialpadVibration: Boolean,
    onDialpadVibrationChange: (Boolean) -> Unit,
    hideDialpadNumbers: Boolean,
    onHideDialpadNumbersChange: (Boolean) -> Unit,
    dialpadBeeps: Boolean,
    onDialpadBeepsChange: (Boolean) -> Unit,

    // Migrating
    onExportCallsClick: () -> Unit,
    onImportCallsClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(org.fossify.commons.R.drawable.ic_arrow_left_vector),
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (canLaunchCallingAccounts) {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    painter = painterResource(org.fossify.commons.R.drawable.ic_three_dots_vector),
                                    contentDescription = "More",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.calling_accounts)) },
                                    onClick = {
                                        showMenu = false
                                        onCallingAccountsClick()
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Section 1: Theme & Appearance
            item {
                PreferenceSectionHeader(title = stringResource(R.string.theme_and_appearance))
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.theme),
                    subtitle = themeText,
                    onClick = onThemeClick
                )
            }
            item {
                PreferenceSwitchItem(
                    title = "Pure Black OLED Mode (#000000)",
                    subtitle = "Use 100% pure black for OLED/AMOLED screens to save battery",
                    checked = oledModeEnabled,
                    onCheckedChange = onOledModeChange
                )
            }
            item {
                PreferenceItem(
                    title = "Display Screen Type",
                    subtitle = if (isOledScreen) "OLED / AMOLED" else "LCD",
                    onClick = onScreenTypeClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.app_icon),
                    subtitle = appIconText,
                    onClick = onAppIconClick
                )
            }

            // Divider
            item {
                PreferenceDivider()
            }

            // Section 2: General settings
            item {
                PreferenceSectionHeader(title = stringResource(R.string.general_settings))
            }
            if (showUseEnglish) {
                item {
                    PreferenceSwitchItem(
                        title = stringResource(org.fossify.commons.R.string.use_english_language),
                        checked = useEnglish,
                        onCheckedChange = onUseEnglishChange
                    )
                }
            }
            if (showLanguage) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.language),
                        subtitle = languageText,
                        onClick = onLanguageClick
                    )
                }
            }
            if (showBlockedNumbers) {
                item {
                    PreferenceItem(
                        title = stringResource(R.string.manage_blocked_numbers),
                        onClick = onBlockedNumbersClick
                    )
                }
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.manage_speed_dial),
                    onClick = onSpeedDialClick
                )
            }
            item {
                PreferenceItem(
                    title = "Device IMEI (*#06#)",
                    subtitle = imeiText,
                    onClick = onImeiClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.change_date_and_time_format),
                    onClick = onChangeDateTimeFormatClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.font_size),
                    subtitle = fontSizeText,
                    onClick = onFontSizeClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.manage_shown_tabs),
                    onClick = onManageTabsClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.default_tab_to_open),
                    subtitle = defaultTabText,
                    onClick = onDefaultTabClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(org.fossify.commons.R.string.on_contact_click),
                    subtitle = onContactClickText,
                    onClick = onContactClickAction
                )
            }

            // Divider
            item {
                PreferenceDivider()
            }

            // Section 3: Main screen
            item {
                PreferenceSectionHeader(title = stringResource(R.string.main_screen))
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.open_dialpad_by_default),
                    checked = openDialpadAtLaunch,
                    onCheckedChange = onOpenDialpadAtLaunchChange
                )
            }

            // Divider
            item {
                PreferenceDivider()
            }

            // Section 4: Calls
            item {
                PreferenceSectionHeader(title = stringResource(R.string.calls))
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.group_subsequent_calls),
                    checked = groupSubsequentCalls,
                    onCheckedChange = onGroupSubsequentCallsChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.show_call_confirmation_dialog),
                    checked = showCallConfirmation,
                    onCheckedChange = onShowCallConfirmationChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(org.fossify.commons.R.string.start_name_with_surname),
                    checked = startNameWithSurname,
                    onCheckedChange = onStartNameWithSurnameChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(org.fossify.commons.R.string.format_phone_numbers),
                    checked = formatPhoneNumbers,
                    onCheckedChange = onFormatPhoneNumbersChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.disable_proximity_sensor),
                    checked = disableProximitySensor,
                    onCheckedChange = onDisableProximitySensorChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.disable_swipe_to_answer),
                    checked = disableSwipeToAnswer,
                    onCheckedChange = onDisableSwipeToAnswerChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.show_incoming_calls_full_screen),
                    checked = alwaysShowFullscreen,
                    onCheckedChange = onAlwaysShowFullscreenChange
                )
            }

            // Divider
            item {
                PreferenceDivider()
            }

            // Section 5: Dialpad
            item {
                PreferenceSectionHeader(title = stringResource(R.string.dialpad))
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.dialpad_vibrations),
                    checked = dialpadVibration,
                    onCheckedChange = onDialpadVibrationChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.hide_dialpad_numbers),
                    checked = hideDialpadNumbers,
                    onCheckedChange = onHideDialpadNumbersChange
                )
            }
            item {
                PreferenceSwitchItem(
                    title = stringResource(R.string.dialpad_beeps),
                    checked = dialpadBeeps,
                    onCheckedChange = onDialpadBeepsChange
                )
            }

            // Divider
            item {
                PreferenceDivider()
            }

            // Section 6: Migrating
            item {
                PreferenceSectionHeader(title = stringResource(R.string.migrating))
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.export_call_history),
                    onClick = onExportCallsClick
                )
            }
            item {
                PreferenceItem(
                    title = stringResource(R.string.import_call_history),
                    onClick = onImportCallsClick
                )
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun PreferenceSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
private fun PreferenceItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PreferenceSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun PreferenceDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 6.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    )
}
