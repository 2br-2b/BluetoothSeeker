# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Run instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Lint
./gradlew lint

# Clean
./gradlew clean
```

## Architecture

BluetoothSeeker is a single-module Android app (minSdk 28, targetSdk 36) built with Kotlin, Jetpack Compose, and Room.

**Stack:** Kotlin 2.0.21 · Compose BOM 2024.09 · Room 2.7.2 · DataStore 1.1.7 · MapLibre 11.5.1 · kotlinx.serialization · KSP

### Data Flow

```
BluetoothEventReceiver (ACL_CONNECTED / ACL_DISCONNECTED broadcast)
    → BluetoothRepository.handleBluetoothIntent()
    → Room DB (TrackedBluetoothDeviceEntity, DeviceEventLogEntity)
      + DataStore (user settings)
      + NotificationManager
    → UI observes via StateFlow / Flow
```

### Layers

- **`data/`** — All non-UI logic
  - `repo/BluetoothRepository` — Core device tracking, event logging, export/import
  - `local/SettingsRepository` — DataStore-backed user preferences
  - `location/AndroidLocationRepository` — GPS/Network/Passive location fetching
  - `location/PlaceLabelRepository` — Reverse geocoding
  - `local/AppDatabase` + `Dao` + `Entities` — Room setup (v1→v2 migration adds `customIcon`)
  - `model/` — Enums (`DeviceEventType`, `LogMode`, `ThemePreference`, `SortMode`, `MapStyle`, `LocationQuality`) and serializable data classes for export

- **`ui/`** — Compose UI
  - `RootViewModel` — Initialization, permissions, onboarding flow
  - `AppViewModel` — Device list state, history filtering, settings writes
  - `BluetoothSeekerRoot` — Main composable (large, contains most screens inline)
  - `Theme`, `Formatting`, `Permissions` — Theming, text formatting utilities, permission helpers

- **`receiver/BluetoothEventReceiver`** — Registered in manifest for Bluetooth ACL events

- **`AppContainer`** — Manual DI singleton; instantiated in `BluetoothSeekerApp`, injected into ViewModels via custom `ViewModelFactory`

### Key Design Notes

- No Hilt/Dagger — dependency injection is manual through `AppContainer`
- Location is fetched asynchronously at event time; quality is tagged (`PRECISE`, `APPROXIMATE`, `LAST_KNOWN`)
- Export/import uses `kotlinx.serialization` with `ExportPayload` as the root object
- MapLibre map styles are runtime-selectable (Liberty, Bright, Positron, Dark, Fiord)
- AMOLED theme support alongside system theme
