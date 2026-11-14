# APhone (fossify fork) — Project Status & Modernization Roadmap

## Overview
**APhone** is a modern, privacy-first Android phone and dialer application developed as a fork of **Fossify Phone**. While retaining all of Fossify's backend telephony, telecom, database, and contact management logic, APhone completely redesigns the front-end with **Jetpack Compose (Material 3)**, edge-to-edge support, and a streamlined user experience.

---

## Current Status (Completed & Verified)

### 1. Jetpack Compose UI Transformation
- **100% Compose `MainActivity`**: Completely eliminated legacy `activity_main.xml` and `ViewPagerAdapter.kt`. Replaced with [MainScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/MainScreen.kt) using Material 3 `Scaffold` and `HorizontalPager`.
- **Full Edge-to-Edge Support**: Properly handles `WindowInsets.systemBars`, status bars, gesture navigation pills, and IME software keyboard insets across all screens and dialogs.
- **Centralized Design System**:
  - [Color.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/theme/Color.kt): Central color palette for accents, call types (incoming, outgoing, missed), and semantic surfaces.
  - [Theme.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/theme/Theme.kt): Dynamic luminance detection (`backgroundColor.luminance() < 0.5f`) ensuring seamless support for both app-level custom color schemes and system-level dark/light modes.

### 2. Streamlined 3-Tab Architecture
Consolidated the old 4-tab interface into a focused, modern 3-tab layout:
1. **Call Tab** ([RecentsScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/RecentsScreen.kt)):
   - Call log with incoming, outgoing, and missed call indicators.
   - Quick call actions, profile detail navigation, and clear history options.
2. **Contacts Tab** ([ContactsScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/ContactsScreen.kt)):
   - Integrated **Favourites** section pinned to the top as a horizontal avatar carousel.
   - Full alphabetical contacts list with fast scroll bar indicator.
   - Clean top bar with Search, Create Contact (+), and Three-Dots menu.
3. **Keypad Tab** ([DialpadScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/DialpadScreen.kt)):
   - Modern numeric dialpad with tactile haptics and tones.
   - Consecutive digit entry fix (allows identical numbers to be entered consecutively).
   - Secret code support (e.g. `*#06#` triggers the device IMEI dialog immediately).
   - Search button omitted on the Keypad top bar to eliminate visual clutter.

### 3. Dedicated Unified Search Screen
- Implemented in [SearchScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/SearchScreen.kt):
  - Modern search bar with Back navigation and Clear ('X') button.
  - Real-time search query matching with primary-color text highlighting.
  - **Contacts First, then Calls**: Displays matching contacts in Section 1, followed by matching call history in Section 2.
  - **Auto-Clearing**: Switching tabs via bottom navigation or swipe immediately exits search and clears the query.

### 4. Modern Material 3 Menus & Dialogs
- **Three-Dots Overflow Menu**:
  - High-contrast, dark-mode-optimized popup with 20.dp rounded corners and subtle border outline.
  - Vector icons for each action (Delete, Sort by, Filter, Settings, About).
  - Subtle dividers separating contextual tab actions from global settings.
- **Modern Dialog Box**:
  - Converted legacy confirmation dialogs into a Material 3 Compose `AlertDialog` with circular icon badges, clear typography, and rounded destructive action buttons.
- **Top Bar Decluttering**:
  - Removed `Clear Call History` icon button from the Call tab top bar; it is neatly accessible in the Three-Dots menu.
  - Removed redundant `Sort by` and `Filter` icon buttons from the Contacts top bar; they are neatly organized inside the Three-Dots menu.

### 5. Settings: Curated Theme & Dual App Icons
- **Theme Selection**:
  - Replaced the legacy Fossify color customizer with a clean theme selector in [SettingsActivity.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/activities/SettingsActivity.kt): **System default**, **Light**, and **Dark**.
  - Toggles `isSystemThemeEnabled` and applies designed Material 3 tones with dynamic luminance detection in [Theme.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/theme/Theme.kt).
- **Dual Launcher Icons**:
  - **Dark mode icon**: Solid black background with white phone handset (`@mipmap/ic_launcher_dark`).
  - **Light mode icon**: Solid white background with black phone handset (`@mipmap/ic_launcher_light`).
  - Managed via [AppIconHelper.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/helpers/AppIconHelper.kt) using Android `activity-alias` runtime switching. Cleaned out all 18 legacy colored aliases from [AndroidManifest.xml](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/AndroidManifest.xml).

### 6. Application Package ID
- **New App ID**: Updated `applicationId` to `com.sudhnsh.APhone` (`com.sudhnsh.APhone.debug` in debug builds) in [gradle.properties](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/gradle.properties) and [build.gradle.kts](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/build.gradle.kts).

---

## Active Architecture & Key Files

| Component | File Path | Status |
| :--- | :--- | :--- |
| **Main Activity** | [MainActivity.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/activities/MainActivity.kt) | Converted to Compose |
| **Main Screen** | [MainScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/MainScreen.kt) | Jetpack Compose M3 |
| **Search Screen** | [SearchScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/SearchScreen.kt) | Jetpack Compose M3 |
| **Recents Screen** | [RecentsScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/RecentsScreen.kt) | Jetpack Compose M3 |
| **Contacts Screen** | [ContactsScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/ContactsScreen.kt) | Jetpack Compose M3 |
| **Dialpad Screen** | [DialpadScreen.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/screens/DialpadScreen.kt) | Jetpack Compose M3 |
| **Color Palette** | [Color.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/theme/Color.kt) | Modern M3 Palette |
| **Theme Engine** | [Theme.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/compose/theme/Theme.kt) | Dynamic Luminance Detection |
| **App Icon Helper** | [AppIconHelper.kt](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/kotlin/org/fossify/phone/helpers/AppIconHelper.kt) | Dark & Light Launcher Icons |

---

## Next Steps / Upcoming Work
1. **Convert In-Call & Active Call Screen (`CallActivity`)**:
   - Migrate [activity_call.xml](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/res/layout/activity_call.xml) to a Compose-based modern active call screen with call controls (mute, speaker, hold, keypad, add call, end call).
2. **Convert Remaining Full-Screen Activities**:
   - Migrate [activity_settings.xml](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/res/layout/activity_settings.xml) (`SettingsActivity`).
   - Migrate [activity_manage_speed_dial.xml](file:///Volumes/External%20SSD/Projects/BetterDesignedPhone/app/src/main/res/layout/activity_manage_speed_dial.xml) (`ManageSpeedDialActivity`).
3. **Convert Modal Dialogs**:
   - Modernize `ChangeSortingDialog` and `FilterContactSourcesDialog` to 100% Compose Material 3 dialogs.
