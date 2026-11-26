# Architecture — Stillness Launcher (`mindful-launcher`)

## Overview

Stillness Launcher is a native Android Home Launcher (`minSdk 26`, `targetSdk 35`, Kotlin + Jetpack Compose + Material 3) designed to reduce mindless phone usage via context-aware app filtering and a 5-second gentle friction pause.

## Package Structure (`app/src/main/java/com/indie/mindfullauncher/`)

| Layer | Files | Responsibility |
|---|---|---|
| Entry | `MainActivity.kt` | Hosts `LauncherViewModel`, `LauncherRoute` (`HOME`, `DRAWER`, `SETTINGS`), `StillnessColorScheme`, and `GentleFrictionDialog` overlay |
| Model | `model/LauncherModels.kt` | `ContextProfile` (`WORK`, `EVENING`, `SLEEP`, `ALL`), `AppInfo`, `QuickNote`, `AppUsageSummary` |
| Domain | `domain/CommandBarEngine.kt` | Pure Kotlin engine for command bar: `! <note>` capture, math expression parser, prefix-prioritized app search (max 12) |
| Data | `data/AppRepository.kt` | Queries installed launcher apps via `PackageManager`, checks/opens default launcher & `UsageStatsManager` permissions, computes today's foreground minutes |
| Data | `data/PreferencesRepository.kt` | `SharedPreferences` (`stillness_prefs`) persistence for auto-context toggle, pinned packages per profile, friction packages, and JSON-serialized `QuickNote` list |
| UI | `ui/LauncherViewModel.kt` | `AndroidViewModel` exposing `StateFlow<LauncherUiState>`, 5-second coroutine countdown (`frictionTimerJob`), command bar & note actions |
| UI | `ui/HomeScreen.kt` | Clock/date, onboarding setup card, quick notes (top 3), dynamic command result / context app list, profile chips, command bar, bottom bar |
| UI | `ui/FrictionDialog.kt` | `GentleFrictionDialog` with 5s countdown, today's usage minutes (if permitted), dismiss & confirm launch actions |
| UI | `ui/AppDrawerAndSettings.kt` | `AppDrawerScreen` (search, pin to profile, toggle 5s friction) and `SettingsScreen` (Zero-Telemetry notice, auto-context switch, system settings links) |

## Architectural Invariants

1. **Zero-Telemetry**: `AndroidManifest.xml` must NEVER declare `android.permission.INTERNET`.
2. **No AccessibilityService**: Gentle friction operates strictly at the launcher launch boundary + `UsageStatsManager`.
3. **Pure Domain Logic**: `CommandBarEngine` must remain pure Kotlin with zero Android framework dependencies so it runs fast on JVM unit tests.
4. **Local-First Persistence**: All state is stored locally in `SharedPreferences` (`stillness_prefs`).
