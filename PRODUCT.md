# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users
Android users who want to reduce doomscrolling and phone addiction without being locked out of their apps by harsh blockers. Secondary benefit for knowledge workers and students who value a distraction-free, time-aware home screen.

## Product Purpose
Stillness Launcher replaces the default Android home screen with a calm, text-first interface that adapts to the time of day and introduces a 5-second mindful pause before opening distracting apps. Success means users noticeably cut mindless app launches and daily screen time while feeling respected rather than restricted.

## Positioning
- **Gentle Friction without AccessibilityService:** Uses the native Home Launcher boundary plus `UsageStatsManager` for a 5-second pause showing today's real usage, avoiding the lag, battery drain, and Play Store policy risks of background blockers.
- **Context-Aware Home Screen:** Automatically shifts visible apps across Work (`08:00-17:30`), Evening (`17:30-22:30`), and Sleep (`22:30-06:00`) instead of showing a static list all day.
- **Zero-Telemetry & No Subscriptions:** Deliberately omits the `INTERNET` permission; core minimalist features are free, with an optional one-time Pro unlock ($2.99–$4.99) and zero recurring subscriptions.

## Operating Context
- Native Android app (`minSdk 26` / Android 8.0+, `targetSdk 35`), built with Kotlin and Jetpack Compose.
- Runs 100% locally on-device with `SharedPreferences` persistence.
- Accessed dozens of times per day in short bursts (checking time, launching an essential app, running quick math/notes in the Command Bar, or pausing before opening social media).

## Capabilities and Constraints
- **Implemented MVP capabilities:**
  - Context-aware Home Screen (`Làm việc`, `Buổi tối`, `Đi ngủ`, `Tất cả`) with automatic time-based switching and manual override chips.
  - Command Bar supporting prefix-prioritized app search, instant math evaluation, and `! <note>` quick to-do capture.
  - 5-second Gentle Friction dialog displaying today's app usage minutes (when `PACKAGE_USAGE_STATS` is granted).
  - Searchable App Drawer with per-context pinning and per-app friction toggles.
- **Constraints:**
  - No `INTERNET` permission (Zero-Telemetry commitment).
  - No `AccessibilityService` or background service dependencies that OEM battery savers kill.
  - Bilingual Vietnamese and English support; OLED dark-first display optimization.
  - Undecided: exact feature split between Free tier and one-time Pro tier ($2.99–$4.99), and whether to add Wi-Fi/location triggers in v2.

## Brand Commitments
- **Name:** Stillness Launcher.
- **Values:** Calm, transparent, zero-telemetry, anti-subscription, respectful of user autonomy.
- **Visual constraint (user-confirmed):** OLED-optimized dark-first presentation.

## Evidence on Hand
- Working Android MVP source in `app/src/main/java/com/indie/mindfullauncher/` and unit-tested `CommandBarEngine`.
- Debug APK builds cleanly (`app/build/outputs/apk/debug/app-debug.apk`).
- No user testimonials, Play Store metrics, or production screenshots yet.

## Product Principles
1. **Friction, not force:** Never lock users out; create a 5-second moment of awareness and let them choose.
2. **Context over clutter:** Show only the apps relevant to the current part of the day.
3. **Speed for intent, pause for impulse:** Essential tools, math, and quick notes are instant via the Command Bar; distracting apps get a calm pause.
4. **Local-first trust:** Zero network permissions, zero analytics, zero accounts.
5. **Fair one-time value:** Respect users' wallets with a single Pro unlock rather than subscription fatigue.

## Accessibility & Inclusion
- Bilingual Vietnamese and English support.
- High-contrast OLED dark-first layout; must maintain readable touch targets and clear typography scaling for Android 8.0+ devices.
