# AGENTS.md

Operating manual for AI coding agents working on **Stillness Launcher** (`mindful-launcher`) — an Android minimal, context-aware home launcher built with Kotlin & Jetpack Compose.

## Startup Workflow

Before writing code:

1. **Confirm working directory** with `pwd` (repo root: `mindful-launcher`).
2. **Read this file** completely.
3. **Read project context** on demand:
   - [PRODUCT.md](PRODUCT.md) — product purpose, principles, constraints
   - [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — package layout, state flow, invariants
4. **Run `./init.sh`** (or `.\init.ps1` on PowerShell) to verify the Gradle build and unit tests are healthy.
5. **Read `feature_list.json`** to see current feature state and dependencies.
6. **Read `progress.md`** and `session-handoff.md` to pick up from the previous session.
7. **Review recent commits** with `git log --oneline -5`.

If baseline verification is failing, repair that first before adding new scope.

## Working Rules

- **One feature at a time**: Pick exactly one unfinished feature from `feature_list.json` whose dependencies are `done`.
- **Verification required**: Never claim done without running Gradle verification commands and observing passing results.
- **Respect product invariants**:
  - **Zero-Telemetry**: Never add `INTERNET` permission or network/analytics SDKs.
  - **No AccessibilityService**: Use standard Home Launcher + `UsageStatsManager` only.
  - **Bilingual & OLED dark-first**: Keep UI strings consistent (Vietnamese/English) and preserve dark-first contrast.
- **Update artifacts**: Before ending a session, update `progress.md`, `feature_list.json`, and `session-handoff.md`.
- **Stay in scope**: Do not modify files unrelated to the active feature.
- **Leave clean state**: Next session must be able to run `./init.sh` immediately.

## Required Artifacts

- `feature_list.json` — Feature state tracker (source of truth)
- `progress.md` — Session continuity log
- `init.sh` — Standard startup and verification path (plus `init.ps1` for PowerShell)
- `session-handoff.md` — Structured handoff for multi-session continuity
- `docs/ARCHITECTURE.md` — Progressive-disclosure architecture reference

## Definition of Done

A feature is done only when ALL of the following are true:

- [ ] Target behavior is implemented
- [ ] Required verification actually ran (`testDebugUnitTest` and `assembleDebug`, plus `connectedDebugAndroidTest` if UI/device behavior changed)
- [ ] Evidence recorded in `feature_list.json` (`evidence` field) and `progress.md`
- [ ] Repository remains restartable from `./init.sh`

## End of Session

Before ending a session:

1. Update `progress.md` with current state, modified files, and verification evidence.
2. Update `feature_list.json` with updated feature `status` and `evidence`.
3. Update `session-handoff.md` with next-session startup notes and any unresolved risks/blockers.
4. Commit with a descriptive message once work is in a safe, verified state.
5. Leave the repo clean enough for the next session to run `./init.sh` immediately.

## Verification Commands

```bash
# Full verification (recommended)
./init.sh
```

Required Gradle checks (use `./gradlew` if present, or cached Gradle 8.13):

- Unit tests: `gradle testDebugUnitTest --no-daemon`
- Debug APK build: `gradle assembleDebug --no-daemon`
- Android Lint: `gradle lintDebug --no-daemon`
- Instrumented UI tests (requires connected Android device/emulator): `gradle connectedDebugAndroidTest --no-daemon`

## Escalation

If you encounter:

- **Architecture decisions**: Consult [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md), otherwise ask the user.
- **Unclear requirements**: Check [PRODUCT.md](PRODUCT.md) (especially undecided Free vs. Pro tier or v2 triggers), otherwise ask the user.
- **Repeated test failures**: Update `progress.md` and `session-handoff.md`, flag for human review.
- **Scope ambiguity**: Re-read `feature_list.json` for dependencies and definition of done.
