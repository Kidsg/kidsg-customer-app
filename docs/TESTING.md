# KidsG Testing Strategy

## 1. Domain Unit Tests
Automated tests are housed in `shared/src/commonTest/`:
* `CartCalculationTest.kt`:
  - Validates empty cart initialization
  - Verifies delivery fee computation for subtotal < free delivery threshold (₹199)
  - Confirms automatic free delivery unlocking when subtotal >= ₹199
  - Validates coupon percentage calculation and maximum discount capping (e.g. `KIDSG50`)
  - Verifies order state transitions and terminal states

Run unit tests via command line:
```powershell
$env:JAVA_HOME="C:\Users\admin\jdks\jdk-21.0.12.1+1"
.\gradlew.bat test
```

---

# KidsG Release Runbook (docs/RELEASE.md)

## 1. Android Release
- Min SDK: 24 (Android 7.0)
- Target SDK: 35 (Android 15)
- R8 / ProGuard minification enabled in `androidApp/build.gradle.kts`
- Resource shrinking enabled

### Build Release Artifacts:
```powershell
$env:JAVA_HOME="C:\Users\admin\jdks\jdk-21.0.12.1+1"

# Production APK
.\gradlew.bat :androidApp:assembleRelease

# Production Google Play App Bundle (AAB)
.\gradlew.bat :androidApp:bundleRelease
```

## 2. iOS Release Preparation
- Bundle Identifier: `com.kidsg.app`
- Shared framework: `shared.framework` generated via `embedAndSignAppleFrameworkForXcode`
- Build on macOS with Xcode:
```bash
./gradlew :shared:linkReleaseFrameworkIosArm64
```
