$ErrorActionPreference = "Stop"

Write-Host "Setting environment variables..." -ForegroundColor Cyan
$env:JAVA_HOME = "C:\Users\amine\AppData\Local\Programs\STM32CubeMX\jre"
$env:ANDROID_HOME = "C:\Users\amine\android-sdk"

Write-Host "Building FootAuction-FC APK with Gradle..." -ForegroundColor Cyan
& .\gradlew.bat :app:assembleDebug

if ($LASTEXITCODE -eq 0) {
    Copy-Item "app\build\outputs\apk\debug\app-debug.apk" "FootAuction-FC.apk" -Force
    Write-Host "`n========================================================" -ForegroundColor Green
    Write-Host "[SUCCESS] APK ready to install!" -ForegroundColor Green
    Write-Host "File location: $((Get-Item 'FootAuction-FC.apk').FullName)" -ForegroundColor Green
    Write-Host "========================================================" -ForegroundColor Green
} else {
    Write-Host "`n[FAILED] APK build encountered an error." -ForegroundColor Red
    exit 1
}
