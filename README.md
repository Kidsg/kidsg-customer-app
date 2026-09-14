# KidsG — Small Supplies. Big Futures.

> **KidsG** is a quick-commerce mobile platform for stationery and school essentials (1st to 12th standard), built with **Kotlin Multiplatform + Compose Multiplatform** targeting **Android and iOS**.

---

## 🎨 Visual Metaphor: "The KidsG Desk"

KidsG creates its own category — a blend of a **stationery playground**, **school companion**, and **fast 15-minute delivery service**.

### Signature Brand Palette
* **KidsG Orange (`#FF7A00`)**: Signature action and brand identity
* **Pure White (`#FFFFFF`)**: Clarity, trust, and clean surfaces
* **Jet Black (`#111111`)**: High contrast typography
* **Stationery Accents**: Accent Yellow (`#FFD166`), Soft Pink (`#FF9ACD`), Sky Blue (`#7DD3FC`), Mint (`#A7F3D0`)

---

## 🌟 Milestone 1: The 5 Hero Experiences

1. **Animated KidsG Splash**: Organic pencil stroke logo reveal with playful mascot celebration.
2. **KidsG Desk Home**: Visual student workspace, time-aware greeting ("Ready for school?"), "What do you need today?" search, sticky note intent modes (*School Mode*, *Exam Mode*, *Create Mode*, *New Term*, *Oops Mode*), and nearby store status.
3. **Discovery & Intent Modes**: The Stationery Wall with distinct visual objects for categories + 1-tap Oops Emergency Drawer.
4. **Product Desk View**: Desk-placed stationery composition, specifications table, ruling/tip variants, pricing, and Add to Bag.
5. **Your School Bag (Cart)**: School backpack packing metaphor with dynamic free-delivery progress bar, coupon drawer, and repository-driven bill breakdown.

---

## 🚀 Quick Start

### Prerequisites
* **JDK 21 LTS**
* **Android SDK 35** (Build Tools 34.0.0+)
* **Gradle 8.9** (included via wrapper)

### Build Commands
```powershell
# Set JAVA_HOME to JDK 21
$env:JAVA_HOME="C:\Users\admin\jdks\jdk-21.0.12.1+1"

# Run domain unit tests
.\gradlew.bat test

# Build Android debug APK
.\gradlew.bat :androidApp:assembleDebug

# Build release APK
.\gradlew.bat :androidApp:assembleRelease
```

---

## 📚 Technical Documentation
* [Architecture Guide](file:///c:/Users/admin/Projects/kidsg-app/docs/ARCHITECTURE.md)
* [Setup & Build Runbook](file:///c:/Users/admin/Projects/kidsg-app/docs/SETUP.md)
* [Environment Configuration](file:///c:/Users/admin/Projects/kidsg-app/docs/ENVIRONMENT.md)
* [API & Backend Contract](file:///c:/Users/admin/Projects/kidsg-app/docs/API_CONTRACT.md)
* [Testing Strategy](file:///c:/Users/admin/Projects/kidsg-app/docs/TESTING.md)
* [Release Runbook](file:///c:/Users/admin/Projects/kidsg-app/docs/RELEASE.md)
