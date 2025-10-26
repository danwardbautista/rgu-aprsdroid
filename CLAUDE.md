# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

APRSdroid is an Android application for Amateur Radio operators written in Scala. It allows reporting positions to the APRS (Automatic Packet Reporting System) network, displaying nearby amateur radio stations, and exchanging APRS messages.

**Language**: Scala 2.11.12 with some Java components
**Build System**: Gradle with gradle-android-scala-plugin
**Package**: org.aprsdroid.app
**License**: GPLv2

## Build Commands

### Initial Setup
```bash
# Clone with submodules
git submodule update --init --recursive

# Configure Google Maps API key (required for map view)
echo "mapsApiKey=AI..." > local.properties
```

### Build and Install

**Using Android Studio** (Recommended):
1. Open the project in Android Studio
2. **Important**: Configure Gradle JDK to use Java 11:
   - Go to **File → Settings → Build, Execution, Deployment → Build Tools → Gradle**
   - Set **Gradle JDK** to Java 11 (not Java 17 or 21)
   - The project is configured in `gradle.properties` to use Java 11
3. Let Gradle sync
4. Build and run as normal Android app (Shift+F10)

**Using Command Line**:
```bash
# Debug build and install
./gradlew installDebug

# Release build and install
./gradlew installRelease

# Clean build
./gradlew clean

# Just build APK without installing
./gradlew assembleDebug
./gradlew assembleRelease
```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

**Build Notes**:
- Full builds take approximately 3 minutes
- Incremental builds can be problematic and may produce non-working APKs
- If you encounter "incompatible Java" errors, ensure Android Studio is using Java 11

## Architecture

### Core Components

**AprsService** (`src/AprsService.scala`)
- Central background service managing APRS operations
- Coordinates location updates, message handling, and packet transmission
- Uses broadcast intents for component communication
- API version: 1 (exposed via `AprsService.API_VERSION_CODE`)

**StorageDatabase** (`src/StorageDatabase.scala`)
- SQLite-based storage with two main tables:
  - `posts`: Transmission log (sent/received packets, errors, info messages)
  - `stations`: Station information (position, symbol, comment, etc.)
- Provides cursors for adapters and distance-based queries

**MessageService** (`src/MessageService.scala`)
- Handles APRS message creation, transmission, and acknowledgments
- Manages message notifications
- Coordinates with AprsService for packet transmission

### Backend Architecture (Modular Connection System)

The backend system has three configurable layers:

1. **Protocol Layer** (`src/tncproto/`)
   - `AprsIsProto`: APRS-IS protocol
   - `KissProto`: KISS TNC protocol
   - `Tnc2Proto`: TNC2 text protocol
   - `KenwoodProto`: Kenwood TNC protocol
   - Each implements packet framing/deframing for its protocol

2. **Link Layer** (`src/backend/`)
   - `TcpUploader`: TCP/IP connections (APRS-IS, TNC over network)
   - `UdpUploader`: UDP transmission
   - `HttpPostUploader`: HTTP POST uploads
   - `BluetoothTnc`: Bluetooth serial connections
   - `UsbTnc`: USB serial connections
   - `AfskUploader`: Audio FSK modulation/demodulation (1200 baud Bell 202)

3. **Backend Configuration**
   - Combinations defined in `AprsBackend.backend_collection`
   - Example: "kiss-bluetooth-tcp" = KISS protocol over Bluetooth
   - Default: "aprsis-tcpip-tcp" (APRS-IS over TCP)
   - Upgrade mapping in `AprsBackend.backend_upgrade` for old configs

### Location Sources (`src/location/`)

Three location strategies (factory pattern in `LocationSource.instanciateLocation`):
- `SmartBeaconing`: Adaptive beaconing based on speed/direction changes
- `PeriodicGPS`: Fixed-interval GPS updates
- `FixedPosition`: Manual coordinate entry

### Activity Hierarchy

See `activities.md` for full inheritance tree. Key activities:
- `APRSdroid`: Main launcher activity
- `HubActivity`: Station list sorted by distance
- `LogActivity`: Packet transmission log
- `MessageActivity`: Individual message conversations
- `StationActivity`: Station details and packet history
- `MapAct`/`GoogleMapAct`: Offline (MapsForge) and online (Google Maps) views
- `PrefsAct`: Preferences using Android PreferenceActivity

### Broadcast Intents

AprsService communicates via broadcasts (defined in `AprsService` object):
- `SERVICE_STARTED`/`SERVICE_STOPPED`: Service lifecycle
- `UPDATE`: New log entry
- `MESSAGE`/`MESSAGETX`: Message received/transmitted
- `POSITION`: Position update received
- `LINK_ON`/`LINK_OFF`/`LINK_INFO`: Connection status

## Development Notes

### Java Version Requirements

**Critical**: This project MUST be built with Java 11 (or Java 8):
- The project uses `JavaVersion.VERSION_1_8` for source/target compatibility
- Gradle MUST use Java 11 JDK (configured in `gradle.properties`)
- Java 17+ will cause build failures due to incompatibility with the Scala plugin and Android bootclasspath configuration
- The `afterEvaluate` block in `build.gradle` configures the Android bootclasspath for the Scala compiler

### Known Build Warnings

**javAPRSlib.jar Version Warning**: You may see warnings like "major version 56 is newer than 55" when building. This is because `libs/javAPRSlib.jar` was compiled with Java 12, but we compile with Java 8 for Android compatibility. This is just a warning and can be safely ignored - the JAR will work fine at runtime.

### Build Configuration Details

**PacketDroid Integration**:
- PacketDroid is a git submodule providing AFSK audio processing
- Source files from `PacketDroid/src` are included in the build
- `PacketDroidActivity.java` has been removed as it's not needed for APRSdroid
- Original symlink placeholder files (`AudioBufferProcessor.java`, `PacketCallback.java`) in `src/` were deleted

**Scala/Java Compilation**:
- The project uses `gradle-android-scala-plugin` version 3.5.1
- Scala compiler is configured in `afterEvaluate` block to access Android SDK classes via `-javabootclasspath`
- Both Scala (.scala) and Java (.java) files coexist in the `src/` directory

### ProGuard
- Both debug and release builds use ProGuard (`minifyEnabled true`)
- Configuration in `proguard.cfg`
- Necessary for Scala on Android to reduce method count

### Version Management
- Version code generated from date: `yyyyMMdd00` (or with `RELEASE_MINOR` property)
- Version name from git tags via grgit plugin
- Build info injected as string resources: `build_revision`, `build_date`, `build_version`

### API Keys and Signing
- **Google Maps API Key**: Must be in `local.properties` as `mapsApiKey`
- **Release Signing**: Configure via gradle properties:
  - `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`
  - `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`

### Native Components
- AFSK audio processing uses native code in `jni/`
- JSoundModem integration for digital modes (submodule)

### Permissions
- Dynamic permissions handled by `PermissionHelper`
- Backend-specific permissions in `AprsBackend.BackendInfo.permissions`
- Location-specific permissions in `LocationSource.getPermissions`

### Package Structure
- Main code: `src/` (Scala + some Java)
- Tests: `test/java/` (unit) and `androidTest/java/` (instrumented)
- Resources: `res/`
- XML preferences: `res/xml/`
- Native libs: `libs/` (JNI compiled binaries)

## Common Patterns

### Preference Access
Use `PrefsWrapper` for type-safe preference access throughout the app.

### Database Queries
`StorageDatabase` provides cursor-returning methods. Use with Android `CursorAdapter` subclasses for ListView/RecyclerView binding.

### Adding a New Backend
1. Create backend class extending `AprsBackend`
2. Add entry to `AprsBackend.backend_collection` with `BackendInfo`
3. Create preference XML in `res/xml/` if needed
4. Specify required permissions and duplex capability

### Adding a New TNC Protocol
1. Extend `TncProto` abstract class
2. Implement `write()` for packet encoding
3. Implement `read()` for packet decoding
4. Register in appropriate backend
