#!/bin/bash
set -e

echo "=== Stillness Launcher Harness Initialization ==="

if [ -x "./gradlew" ]; then
  GRADLE_CMD="./gradlew"
elif command -v gradle >/dev/null 2>&1; then
  GRADLE_CMD="gradle"
else
  CACHED_GRADLE=$(find "${USERPROFILE:-$HOME}/.gradle/wrapper/dists" -name "gradle" -o -name "gradle.bat" 2>/dev/null | grep "gradle-9.3.1" | head -n 1 || true)
  if [ -n "$CACHED_GRADLE" ]; then
    GRADLE_CMD="$CACHED_GRADLE"
  else
    echo "ERROR: Neither ./gradlew, global gradle, nor cached Gradle 8.13 found."
    exit 1
  fi
fi

echo "=== Using Gradle: $GRADLE_CMD ==="
echo "=== Running Unit Tests (testDebugUnitTest) ==="
"$GRADLE_CMD" testDebugUnitTest --no-daemon

echo "=== Building Debug APK (assembleDebug) ==="
"$GRADLE_CMD" assembleDebug --no-daemon

echo "=== Verification Complete ==="
echo ""
echo "Next steps:"
echo "1. Read feature_list.json to see current feature state"
echo "2. Read progress.md and session-handoff.md"
echo "3. Pick ONE unfinished feature whose dependencies are done"
echo "4. Re-run ./init.sh (or .\init.ps1) before claiming done"
