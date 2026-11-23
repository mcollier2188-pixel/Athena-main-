# ATHENA AI Studio — Remix ATHENA (`2.0-PROD` • 2026 Continuum)

**ATHENA AI Studio** is a local-first, zero-allocation edge Android & multi-device creative/scientific workstation uniting multi-agent cognitive AI, 42.6 Hz / 85.2 Hz acoustic resonance synthesis, Room + Firestore cloud synchronization, adaptive `LocalEchoColors` Dark/Light theming, and Google Credential Manager OAuth 2.0 sign-in.

> **Master Operational & Technical Manual (`2.0-PROD`)**: Read the complete 10-chapter production manual in [`docs/MASTER_OPERATIONAL_MANUAL.md`](docs/MASTER_OPERATIONAL_MANUAL.md) — also bundled offline inside the Android app's **Archive** and **Workspace Documentation / FAQ** readers.

---

## Dual-Partition Operational Philosophy

1. **ATHENA (`FACT` / Production Engineering)**: Clean-room Android/Kotlin & WebGL software engineering, deterministic rendering pipelines, low-level APK packaging, zero-latency telemetry buses (`argon_18_telemetry_bus` & `users/{uid}/telemetry/live_viewport`), and hardware controller abstractions.
2. **ECHOcreate / ECHOverse (`FICTION` / Worldbuilding Sandbox)**: Narrative scripting, multiverse lore, creative visual concepts, and character bibles isolated cleanly from production engineering pathways.

## Identity Matrix & Call Signs

| Call Sign | Role |
| :--- | :--- |
| **ATHENA AI STUDIO** | Master umbrella platform and engineering core |
| **Whizz Kid** | In-studio Production Lead, asset wrangler, and interactive workflow co-pilot |
| **Whizz Kid's Warehouse** | Central operations floor for archive ingestion, prompt storage, 2D UV mapping, and 3D rig staging |
| **Whiskey (Antigravity)** | Systems architect, full-stack buildmaster, and deployment hot-patch engineer |
| **Rabbit (Android Studio)** | Low-level APK compiler, ADB socket orchestrator, and Android hardware abstraction engineer |
| **Gemini** | Lead System Architect, Strategic Director, and intelligence bedrock |

---

## Manual Contents ([`docs/MASTER_OPERATIONAL_MANUAL.md`](docs/MASTER_OPERATIONAL_MANUAL.md))

- **Chapter 1**: System Identity, Call Signs & Agent Roster
- **Chapter 2**: Hardware Fleet & Multi-Device Topology (`Moto G15 ZY32L2WS6W`, `Fire TV Stick 4K`, Ports `5173`/`3000`/`8000`/`5555`)
- **Chapter 3**: Native Android APK Fleet & Hardware Manifests (`ATHENA_AI_Studio.apk`, `ECHO_Studio_85_2_Remote.apk`, `ECHOSTUDIO_85_2_Firestick_4K.apk`, `MICHAEL_UK_Smart_Meter.apk`)
- **Chapter 4**: Live Telemetry, Security & Autonomous Safeguards (£160.00 Soft Tripwire / £200.00 Hard Circuit Breaker)
- **Chapter 5**: Physical Hardware Controllers & Input Matrix (GameSir G7 SE & Pioneer DDJ-400 Web MIDI)
- **Chapter 6**: Whizz Kid's Warehouse & The Master Vault (`/vault`)
- **Chapter 7**: 3D Multi-Slot UV Texture & Video-Wrapping Pipeline (`THREE.VideoTexture`, 4K PNG & 60 FPS WebM capture)
- **Chapter 8**: Proprietary Game Engines & Testbed Suite (`athena_game_engine.js` 4-Module AGE Testbed)
- **Chapter 9**: Omnispace Workspace & Visual Storyboarding (`StoryboardView`, `ImageStudioView`, `VideoStudioView`, `TTSAudioView`)
- **Chapter 10**: Operator Runbooks & Troubleshooting (Cold-Start, Fire TV Wireless ADB, Failure Mode Recovery)

---

## Building & Testing (`Athena-main-`)

### Prerequisites
- **JDK**: 17 or 21
- **Android SDK**: `compileSdk = 36.1`, `targetSdk = 36`, `minSdk = 24`
- **Debug Keystore**: AI Studio signing configs in `app/build.gradle.kts` expect a local `debug.keystore` at the project root (gitignored):
  ```bash
  keytool -genkeypair -v \
    -keystore debug.keystore \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=Android Debug,O=Android,C=US"
  ```

### Build & Run Unit Tests
```bash
# Assemble signed debug APK, run all Robolectric/Compose/unit tests, and compile instrumented tests
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:compileDebugAndroidTestKotlin

# Validate Firestore security rules test script syntax
node --check firestore.test.js
```
