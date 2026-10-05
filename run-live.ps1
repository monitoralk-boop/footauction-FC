$ErrorActionPreference = "Stop"

$env:JAVA_HOME = "C:\Users\amine\AppData\Local\Programs\STM32CubeMX\jre"
$env:ANDROID_HOME = "C:\Users\amine\android-sdk"
$adb = "C:\Users\amine\android-sdk\platform-tools\adb.exe"

Write-Host "Checking for connected Android device..." -ForegroundColor Cyan
$devices = & $adb devices | Where-Object { $_ -match "\bdevice\b" -and $_ -notmatch "List of devices" }

if (-not $devices) {
    Write-Host "[WARNING] No Android device detected via ADB!" -ForegroundColor Yellow
    Write-Host "To connect your phone:" -ForegroundColor Yellow
    Write-Host " 1. Enable Developer Options on your phone (tap Build Number 7 times)." -ForegroundColor Yellow
    Write-Host " 2. Enable USB Debugging in Settings > Developer Options." -ForegroundColor Yellow
    Write-Host " 3. Plug your phone into your PC with a USB cable and tap 'Allow' on phone." -ForegroundColor Yellow
    Write-Host "`nRebuilding APK file only..." -ForegroundColor Cyan
    & .\gradlew.bat :app:assembleDebug
    Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "FootAuction-FC.apk" -Force
    Write-Host "[SUCCESS] Updated FootAuction-FC.apk" -ForegroundColor Green
    exit 0
}

Write-Host "Connected device found: $devices" -ForegroundColor Green
Write-Host "Building and installing updated app live to phone..." -ForegroundColor Cyan

& .\gradlew.bat :app:installDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host "Launching FootAuction FC on phone..." -ForegroundColor Cyan
    & $adb shell am start -n com.aistudio.footauction.kxmpzq/com.example.MainActivity
    Write-Host "[SUCCESS] Live update complete and app running on phone!" -ForegroundColor Green
} else {
    Write-Host "[FAILED] Build or install failed." -ForegroundColor Red
}
