$ErrorActionPreference = "Stop"

Write-Host "=== Stillness Launcher Harness Initialization ==="

if (Test-Path ".\gradlew.bat") {
    $GradleCmd = ".\gradlew.bat"
} elseif (Get-Command gradle -ErrorAction SilentlyContinue) {
    $GradleCmd = "gradle"
} else {
    $CachedGradle = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists" -Recurse -Filter "gradle.bat" -ErrorAction SilentlyContinue |
        Where-Object { $_.FullName -match "gradle-9\.3\.1" } |
        Select-Object -First 1 -ExpandProperty FullName
    if ($CachedGradle) {
        $GradleCmd = $CachedGradle
    } else {
        Write-Error "Neither .\gradlew.bat, global gradle, nor cached Gradle 8.13 found."
        exit 1
    }
}

Write-Host "=== Using Gradle: $GradleCmd ==="
Write-Host "=== Running Unit Tests (testDebugUnitTest) ==="
& $GradleCmd testDebugUnitTest --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Building Debug APK (assembleDebug) ==="
& $GradleCmd assembleDebug --no-daemon
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "=== Verification Complete ==="
Write-Host ""
Write-Host "Next steps:"
Write-Host "1. Read feature_list.json to see current feature state"
Write-Host "2. Read progress.md and session-handoff.md"
Write-Host "3. Pick ONE unfinished feature whose dependencies are done"
Write-Host "4. Re-run .\init.ps1 before claiming done"
