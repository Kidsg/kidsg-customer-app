# KidsG Production Release Guide

## 1. Android Release Configuration
- **Application ID:** `com.kidsg.app`
- **Version Code:** `1`
- **Version Name:** `1.0.0`
- **Minimum SDK:** `24`
- **Target SDK:** `35`

### Release Build Commands
```powershell
$env:JAVA_HOME="C:\Users\admin\jdks\jdk-21.0.12.1+1"

# Assemble Release APK:
.\gradlew.bat :androidApp:assembleRelease

# Bundle Android App Bundle (AAB):
.\gradlew.bat :androidApp:bundleRelease
```

Artifact outputs:
- APK: `androidApp/build/outputs/apk/release/androidApp-release.apk`
- AAB: `androidApp/build/outputs/bundle/release/androidApp-release.aab`

## 2. Signing Credentials
Store keystore passwords and alias outside version control in local environment variables:
- `KIDSG_RELEASE_STORE_FILE`
- `KIDSG_RELEASE_KEY_ALIAS`
- `KIDSG_RELEASE_STORE_PASSWORD`
- `KIDSG_RELEASE_KEY_PASSWORD`

## 3. iOS Production Build
On macOS with Xcode 15+:
```bash
./gradlew :shared:assembleSharedReleaseXCFramework
xcodebuild -workspace iosApp/iosApp.xcworkspace -scheme iosApp -configuration Release archive -archivePath build/KidsG.xcarchive
```
