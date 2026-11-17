# APhone (fossify fork)

<img alt="Logo" src="graphics/icon.webp" width="120" />

**APhone** is a modern, privacy-first Android dialer and phone application, developed as a design-focused **fork of Fossify Phone**.

All the robust, battle-tested backend architecture that Fossify provides—telephony services, Telecom framework integration, SIM management, call handling, private contacts storage, and permissions—remains completely intact. This fork fundamentally transforms the **design, user experience, and overall UI structure**, rebuilding the entire front-end in **Jetpack Compose with Material 3** and introducing authentic **Neumorphism**.

---

## 📸 Screenshots

### Dark / OLED Mode
| Keypad (Neumorphic) | Call / Recents | Contacts | Settings (Compose) |
|:---:|:---:|:---:|:---:|
| <img src="graphics/screenshots/keypad.png" width="220" /> | <img src="graphics/screenshots/call_recents.png" width="220" /> | <img src="graphics/screenshots/contacts.png" width="220" /> | <img src="graphics/screenshots/settings.png" width="220" /> |

### Light Mode
| Keypad (Soft UI) | Call / Recents | Contacts | Settings |
|:---:|:---:|:---:|:---:|
| <img src="graphics/screenshots/keypad_light.png" width="220" /> | <img src="graphics/screenshots/call_recents_light.png" width="220" /> | <img src="graphics/screenshots/contacts_light.png" width="220" /> | <img src="graphics/screenshots/settings_light.png" width="220" /> |

---

## 🎨 What Changes in this Fork

While every backend component from Fossify is retained, the user-facing interface and navigation structure are re-engineered from the ground up:

- **100% Declarative UI with Jetpack Compose**: Replaced legacy XML layouts and ViewPagers in `MainActivity` with Material 3 Jetpack Compose.
- **Authentic Neumorphism (Soft UI)**: Dialpad keys (0–9, *, #, and Call) sculpted with dual-shadow canvas rendering (`frameworkPaint.setShadowLayer`), seamlessly blending into the screen background with zero hard borders.
- **Floating Pill Bottom Navigation Bar**: Capsule navigation pill featuring smooth animated sliding transitions, icon scaling, and landscape/portrait adaptation.
- **Streamlined 3-Tab Architecture**:
  1. **Call**: Recents and call log with call type indicators, durations, and one-tap calling.
  2. **Contacts**: Full contacts directory with multiselect bulk deletion, search, and fast alphabet scroller.
  3. **Keypad**: Modern numeric dialpad with haptic feedback, secret code support (`*#06#` for instant IMEI display), and an uncluttered top bar.
- **Contact Multiselect & Bulk Deletion**: Long-press any contact to activate multiselect mode with individual checkboxes, select all/none, and secure batch deletion.
- **Modern Compose Settings**: Rebuilt Settings screen featuring **Pure Black OLED Mode (#000000)**, display screen type switching, and device IMEI information.
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
