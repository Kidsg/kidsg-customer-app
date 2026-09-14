# KidsG Setup & Build Guide

## 1. System Requirements
- **Operating System:** Windows 10/11, macOS, or Linux
- **Java Development Kit:** JDK 21 LTS (e.g. Eclipse Temurin or Microsoft OpenJDK 21)
- **Android SDK:** API 35 installed with Android Studio
- **iOS Toolchain:** macOS with Xcode 15+ (for iOS build)

## 2. Environment Variables
Ensure `JAVA_HOME` points to your JDK 21 installation:
```powershell
$env:JAVA_HOME="C:\Users\admin\jdks\jdk-21.0.12.1+1"
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
```

Verify in `local.properties`:
```properties
sdk.dir=C\:\\Users\\admin\\AppData\\Local\\Android\\Sdk
```

## 3. Building the Application

### Verify Project Structure
```powershell
.\gradlew.bat projects
```

### Run Automated Unit Tests
```powershell
.\gradlew.bat test
```

### Build Android APK
```powershell
.\gradlew.bat :androidApp:assembleDebug
```
The resulting debug APK will be generated at:
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### Build Android Release Bundle (AAB)
```powershell
.\gradlew.bat :androidApp:bundleRelease
```
The resulting release bundle will be generated at:
`androidApp/build/outputs/bundle/release/androidApp-release.aab`
