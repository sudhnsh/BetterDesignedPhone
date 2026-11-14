# APhone (fossify fork)

<img alt="Logo" src="graphics/icon.webp" width="120" />

**APhone** is a modern, privacy-first Android dialer and phone application, developed as a design-focused **fork of Fossify Phone**.

All the robust, battle-tested backend architecture that Fossify provides—telephony services, Telecom framework integration, SIM management, call handling, private contacts storage, and permissions—remains completely intact. This fork fundamentally transforms the **design, user experience, and overall UI structure**, rebuilding the entire front-end in **Jetpack Compose with Material 3**.

---

## 🎨 What Changes in this Fork

While every backend component from Fossify is retained, the user-facing interface and navigation structure are re-engineered from the ground up:

- **100% Declarative UI with Jetpack Compose**: Replaced legacy XML layouts and ViewPagers in `MainActivity` with Material 3 Jetpack Compose.
- **Streamlined 3-Tab Architecture**:
  1. **Call**: Recents and call log with call type indicators, durations, and one-tap calling.
  2. **Contacts**: Full contacts directory with **Favourites pinned as a horizontal carousel** at the top.
  3. **Keypad**: Modern numeric dialpad with haptic feedback, fixed number repetition, secret code support (`*#06#` for instant IMEI display), and an uncluttered top bar.
- **Unified Search Experience**: Tapping search opens a dedicated search screen displaying matching **Contacts first**, followed by matching **Call History**, with automatic dismissal when switching tabs.
- **Modern Material 3 Menus & Dialogs**:
  - Three-dots overflow menu with 20.dp rounded corners, dark theme container styling (`#1E1E22`), and crisp vector icons.
  - Native Compose `AlertDialog` confirmation dialogs with circular icon badges and styled action buttons.
  - Cleaned-up top bar: Sort by and Filter icons removed from the header and neatly housed inside the three-dots menu.
- **Edge-to-Edge & System Insets**: Native support for status bars, navigation bars, gesture navigation pills, and IME software keyboards across all screens.
- **Centralized Design System**: Dedicated [Color.kt](app/src/main/kotlin/org/fossify/phone/compose/theme/Color.kt) palette and dynamic luminance-based theme detection in [Theme.kt](app/src/main/kotlin/org/fossify/phone/compose/theme/Theme.kt).

---

## 📱 Architecture & Backend Continuity

APhone preserves the core telephony engine from Fossify:
- `CallService` & Telecom subsystem integration
- `CallNotificationManager` and heads-up call alerts
- `RecentsHelper` & Call log database operations
- `ContactsHelper` & Android Contacts Provider sync
- `SimpleCallScreeningService` for call blocking and spam filtering
- Offline and private contacts management

For comprehensive technical status, file mappings, and modernization roadmap, see **[STATUS.md](STATUS.md)**.
