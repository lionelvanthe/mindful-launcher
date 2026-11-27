# Session Progress Log

## Current State

**Last Updated:** 2026-11-27 12:52
**Active Feature:** `feat-008` (Active Anti-Addiction Gentle Friction System) Complete — Next: `feat-007` (Pro Unlock & v2 Context Triggers once product decisions are finalized)

## Status

### What's Done

- [x] Integrated Harness Engineering structure based on `walkinglabs/learn-harness-engineering`
- [x] Created `AGENTS.md` with startup workflow, working rules, invariants, and verification commands
- [x] Created `docs/ARCHITECTURE.md` for progressive disclosure of `mindful-launcher` layers
- [x] Created `feature_list.json` tracking MVP features (`feat-001`..`feat-004` done) and upcoming features (`feat-005`..`feat-007`)
- [x] Created `init.sh` and `init.ps1` verification entrypoints (`testDebugUnitTest` + `assembleDebug`)
- [x] Created `session-handoff.md` for multi-session continuity

### What's In Progress

- [ ] None (clean handoff state)

### What's Next

1. Pick `feat-005` (`Automated UI and Repository Test Coverage`) or `feat-006` (`Fix AppDrawerScreen Nested Scroll Layout`).
2. Run `./init.sh` (or `.\init.ps1`) at session start to confirm baseline health.

## Blockers / Risks

- [ ] **No `gradlew` wrapper jar/script in repo**: `gradle/wrapper/gradle-wrapper.properties` exists, but `gradlew` / `gradlew.bat` are not committed. `init.sh` and `init.ps1` handle this by falling back to cached Gradle 8.13.
- [ ] **Nested scroll in `AppDrawerScreen`**: `AppDrawerAndSettings.kt` currently nests a `LazyColumn` inside a `Column` with `.verticalScroll(...)`, which should be fixed in `feat-006`.

## Decisions Made

- **Harness follows `learn-harness-engineering` five-subsystem model**: Instructions (`AGENTS.md`, `docs/ARCHITECTURE.md`), State (`feature_list.json`, `progress.md`), Verification (`init.sh`, `init.ps1`), Scope (`feature_list.json` dependencies), Lifecycle (`session-handoff.md`).
- **Kept Android app source untouched during harness setup**: Harness integration adds only agent operating files and verification scripts without altering app runtime behavior.

## Files Added This Session

- `AGENTS.md` — Agent operating manual and startup/wrap-up protocol
- `docs/ARCHITECTURE.md` — Architecture map and invariants
- `feature_list.json` — Machine-readable feature tracker
- `init.sh` — Cross-platform bash initialization & verification script
- `init.ps1` — PowerShell initialization & verification script
- `progress.md` — Session progress log
- `session-handoff.md` — Next-session handoff template/state

## Evidence of Completion

- [x] Baseline unit tests & debug APK build pass via `.\init.ps1`
- [x] Fixed startup crash on API 26–28 (verified on `Mi_MIX_2` / Android 9 API 28): guarded `AppOpsManager.unsafeCheckOpNoThrow` with `Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q` and fell back to `checkOpNoThrow` in `AppRepository.kt`
- [x] Fixed `feat-006` nested scroll crash in `AppDrawerScreen` (`AppDrawerAndSettings.kt`): removed outer `.verticalScroll(rememberScrollState())` and bounded `LazyColumn` with `Modifier.weight(1f).fillMaxWidth()` + `imePadding()`
- [x] Verified `.\init.ps1`, `gradle lintDebug --no-daemon`, and live AppDrawer navigation on device `1f5efaa4`
- [x] Completed Niagara-inspired UI Polish (Parts 1–4): bundled OFL `kumbh_sans_vf.ttf` & `nunito_sans_vf.ttf`, added `StillnessTheme.kt` (`StillnessTypography`), upgraded `AtAGlanceHeader` and pill Command Bar in `HomeScreen.kt`, added `StillnessEffects.kt` (`pressScale`, `verticalFadingEdge`, `AlphabetScrubber`), and integrated `AlphabetScrubber` + theme tokens in `AppDrawerAndSettings.kt`; verified via `.\init.ps1` and `lintDebug` (BUILD SUCCESSFUL)
- [x] Rebuilt `HomeScreen.kt` as a pure Niagara-style single-screen launcher (Favorites + inline A-Z Alphabet Wave + real `AppIcon` with LRU cache in `AppIcon.kt` + long-press `NiagaraAppOptionsDialog` for Pin/App Info/Uninstall); fixed `AlphabetScrubber` initial `coerceIn` range and `AppIcon` lint; verified via `.\init.ps1` and `lintDebug` (BUILD SUCCESSFUL)
- [x] Completed Niagara Phase 2: added `LauncherFolder` pop-ups (`NiagaraFolderPopupDialog`, `AddToFolderDialog`), local `READ_CALENDAR` At-a-Glance event preview, `NiagaraNotificationListener` inline notification previews (`NotificationRepository.kt`), transparent wallpaper mode (`FLAG_SHOW_WALLPAPER`), and JSON Backup/Restore in `PreferencesRepository.kt` & `AppDrawerAndSettings.kt`; verified via `.\init.ps1` and `lintDebug` (BUILD SUCCESSFUL)
- [x] Completed Full Offline Niagara Suite: added `MediaPlaybackState` At-a-Glance controls (`NotificationRepository.kt`), interactive `NotificationPopupDialog` with `RemoteInput` quick reply & `NotificationSummaryDialog` (`HomeDialogs.kt`), `LauncherApps` App Shortcuts (`AppRepository.kt`), `WidgetHostHelper.kt` (`AppWidgetHost`), `IconPackHelper.kt` + monochrome icons (`AppIcon.kt`), `UsageStatsManager` Smart Sort & Often-Used apps, and customizable Niagara FAB (`NiagaraFabActionDialog`); verified via `.\init.ps1`, `lintDebug`, and live launch on `emulator-5554`
- [x] Added final offline Niagara parity features & discoverability: `READ_CONTACTS` Contact Search + dialer (`AppRepository.kt`), 7-day `CalendarAgendaDialog`, per-app `RenameAppDialog`, `FontPreset` switcher (`StillnessTheme.kt`), `AudioManager` headphone music app suggestions, 2-column large-screen layout (`HomeScreen.kt`), per-app Pop-up Widgets (`HomeDialogs.kt`), and `QuickTipsDialog` + tappable notification pills; verified via `.\init.ps1`, `lintDebug`, and `emulator-5554` install
- [x] Analyzed `Niagara+Launcher+‧+Home+Screen_1.16.38_APKPure.apk` via Craft Anything MCP (`android_package_show`, `android_resources_read`, `android_classes_find`, `android_class_show`) and aligned clean-room UI/UX gaps: Alphabet Wave (`64dp` wave offset, `44dp` elevated `28sp` handle, `19dp` max section height), Typography (`16sp` primary row labels, `20sp` A–Z section headers, `48sp` default clock), 7 At-a-Glance clock styles (`CLASSIC`, `HORIZONTAL`, `VERTICAL`, `DAY_OF_WEEK`, `MINIMALIST`, `COMPACT`, `NONE`), Niagara Accent Color presets (`Niagara Blue` default + Amber/Green/Lavender/Coral), top-anchored `18sp` search bar, frameless inline notification previews, `36dp` rounded bottom sheets, `52dp` row targets, and `56dp` FAB; verified via `.\init.ps1` (`testDebugUnitTest` + `assembleDebug`) and `lintDebug` (all `BUILD SUCCESSFUL`)
- [x] Completed remaining offline Niagara 1.16.38 features from full APK subpackage audit (`ev_8e4d9a40`, `ev_3f85d9f8`): added remaining 3 clocks (`ANALOG`, `FLIP`, `OVERLAP` → 10/10 total), `IconShapePreset` (`DEFAULT`, `CIRCLE`, `SQUIRCLE`, `DOTS` Niagara Dots in `AppIcon.kt`), custom-ordered Favorites with Move Up/Move Down (`PreferencesRepository.kt`, `HomeDialogs.kt`), `ScreenTimeUsageDialog` (7-day local `UsageStatsManager` breakdown), `NiagaraWidgetStack` (`HorizontalPager` with worm-dot indicators), 2×2 mini-icon folder preview + inline folder rename, swipe-down to expand notification shade (`EXPAND_STATUS_BAR`), Hide Status Bar toggle (`WindowInsetsControllerCompat`), and `/coin` + `/dice` secret command utilities (`CommandBarEngine.kt` + unit tests); verified via `.\init.ps1` and `lintDebug` (all `BUILD SUCCESSFUL`)
- [x] Completed `feat-005` (`Automated UI and Repository Test Coverage`): added Compose UI test deps (`ui-test-junit4`, `ui-test-manifest`, `androidx.test.ext:junit`, `androidx.test:runner`), created `StillnessLauncherUiTest.kt` (HomeScreen ordered favorites & `/coin` utility, Top Search FAB, GentleFrictionDialog countdown & confirm, SettingsScreen Niagara options & Zero-Telemetry) and `PreferencesRepositoryInstrumentedTest.kt` (clocks/accent/icon shape/hide status bar, ordered favorites, folder CRUD, widget stack & JSON backup round-trip); verified `testDebugUnitTest`, `connectedDebugAndroidTest` (**9/9 passed on `JSN-L22` Android 10**), and `assembleDebug` (all `BUILD SUCCESSFUL`)
- [x] Completed `feat-008` (`Active Anti-Addiction Gentle Friction System`): wired `HomeScreen` `onAppClick` through `vm::onAppClicked`, added `opensToday` via `UsageEvents` + local daily fallback and `resistedToday` daily counter (`AppRepository.kt`, `PreferencesRepository.kt`), redesigned `GentleFrictionDialog` with pulsing breathing circle (`Hít vào...` / `Thở ra...`), 1-tap intention chips, open-count & resisted-count stats (`FrictionDialog.kt`), added Home long-press friction toggle (`HomeDialogs.kt`), and added `PostSessionNudge` reflection card on resume (`LauncherViewModel.kt`, `HomeScreen.kt`, `MainActivity.kt`); verified via `.\init.ps1` (`testDebugUnitTest` + `assembleDebug`), `lintDebug`, and `connectedDebugAndroidTest` (**10/10 passed on `emulator-5554`**)

## Notes for Next Session

Start by running `.\init.ps1` (or `./init.sh`), then read `feature_list.json` and pick `feat-005` or `feat-006`.
