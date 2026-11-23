# ATHENA AI STUDIO // MASTER OPERATIONAL & TECHNICAL MANUAL
**Document Version:** 2.0-PROD • **Target Release:** 2026 Continuum  
**System Architecture:** Local-First / Zero-Allocation Edge / Decoupled WebGL & Telemetry  
**Security Classification:** FACT / ATHENA Production Standard  
**Authors/Principals:** Michael Collier, Gemini (Lead Architect), Whiskey (Antigravity Systems Lead), Whizz Kid (Production Lead), Rabbit (Android Runtime Lead)

---

## CHAPTER 1: SYSTEM IDENTITY, CALL SIGNS & AGENT ROSTER

### 1.1 Platform Namespace & Operational Philosophy
ATHENA AI Studio operates under a strict dual-partition architecture:
1. **ATHENA (FACT / Production Engineering)**: Clean-room software engineering, deterministic rendering pipelines, low-level Android APK packaging, zero-latency local telemetry buses, and rock-solid hardware controller abstractions. All production builds are verified against real operational hardware.
2. **ECHOcreate / ECHOverse (FICTION / Worldbuilding Sandbox)**: Narrative scripting, multiverse lore, creative visual concepts, and character bibles. Quarantined cleanly inside isolated asset pathways (`ECHOcreate/`) so creative media never pollutes or destabilizes the production architecture.

### 1.2 Identity Matrix & Call Signs
* **ATHENA AI STUDIO**: The master umbrella platform and engineering core.
* **Whizz Kid**: The in-studio Production Lead, asset wrangler, and interactive workflow co-pilot.
* **Whizz Kid's Warehouse**: Central operations floor for archive ingestion, prompt storage, 2D UV mapping, and 3D rig staging.
* **Whiskey (Antigravity)**: Systems architect, full-stack buildmaster, and deployment hot-patch engineer.
* **Rabbit (Android Studio)**: Low-level APK compiler, ADB socket orchestrator, and Android hardware abstraction engineer.
* **Gemini**: Lead System Architect, Strategic Director, and intelligence bedrock.

### 1.3 Master Brand Assets & Display Gates
The suite enforces visual branding across three dedicated emblems:
* **Echo Studio 85.2 Radial Dial (`echo_studio_85_2_circular.jpg`)**: Notched spectrum dial used as the login gate, mobile PWA badge, and icon for `ECHO_Studio_85_2_Remote.apk` (`com.athena.echoremote`).
* **ATHENA AI Studio Owl Crest (`athena_ai_studio_owl.jpg`)**: High-contrast cybernetic owl crest serving as the master studio desktop identity and official icon for `ATHENA_AI_Studio.apk` (`com.athena.aistudio`).
* **ECHOSTUDIO (85.2) Landscape Banner (`echostudio_85_2_banner.jpg`)**: 16:9 widescreen display crest rendered as the native Android TV Leanback banner (`@drawable/tv_banner`) for `ECHOSTUDIO_85_2_Firestick_4K.apk` (`com.athena.display4k`).

---

## CHAPTER 2: HARDWARE FLEET & MULTI-DEVICE TOPOLOGY

### 2.1 Target Device Fleet
* **Primary Workstation (PC / Laptop)**: Local host running the Vite 6 production server, WebGL Three.js renderers, Web MIDI subsystems, and local telemetry broadcasting.
* **Motorola Moto G15 (`ZY32L2WS6W`)**: Handheld mobile companion node connected via high-speed USB/Wi-Fi ADB, hosting the remote control deck and smart meter logger.
* **Amazon Fire TV Stick 4K (`192.168.1.209`)**: Wireless large-format display target receiving hardware-accelerated 60 FPS viewport telemetry and running the native leanback display APK.
* **Secondary Laptop / External Display Monitors**: Borderless standalone pop-out windows mirroring live 3D viewports with zero-latency desktop bus coupling.

### 2.2 Localhost & Edge Networking
* **Port `5173`**: Primary Vite/React studio application running on `http://localhost:5173`.
* **Port `3000`**: Local cloud emulate/function bridge.
* **Port `8000`**: Static media asset server and video streaming socket.
* **Port `5555`**: TCP ADB socket for over-the-air Fire TV Stick 4K control.

### 2.3 The "Batches as Patches" Deployment Model
To ensure 100% stability on client hardware, native Android APK containers are compiled once and locked. The native shells embed hardware-accelerated WebView containers with local storage and WebGL 2.0 flags enabled. All application features, 3D engines, and UI layers are deployed over-the-air (OTA) via Vite builds pushed directly to Firebase Hosting (`https://comic-book-companion-to-flow.web.app`). This completely avoids redundant APK recompilation cycles.

### 2.4 Multi-Monitor Launcher Scripts
* **`OPEN_ATHENA_MASTER_VAULT.bat`**: Single-click script on the Desktop that validates port 5173 and boots the Master Vault directly in the workstation's default browser.
* **`LAUNCH_2ND_SCREEN_POPOUT.bat`**: Boots a dedicated, frameless Chrome application window pointing to `http://localhost:5173/display` sized to 1280x720 for effortless multi-monitor positioning.

---

## CHAPTER 3: NATIVE ANDROID APK FLEET & HARDWARE MANIFESTS

### 3.1 `ATHENA_AI_Studio.apk` (`com.athena.aistudio`)
* **Chassis Role**: Master studio tablet and desktop-mode APK.
* **Permissions**: `INTERNET`, `ACCESS_NETWORK_STATE`, `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `BLUETOOTH`, `BLUETOOTH_CONNECT`.
* **Manifest Flags**: Hardware acceleration forced `true`, multi-window support enabled.

### 3.2 `ECHO_Studio_85_2_Remote.apk` (`com.athena.echoremote`)
* **Chassis Role**: Low-latency handheld remote control deck for smartphones.
* **Features**: Dynamic 8-pad trigger matrix, rig material cyclers, and instant single-tap fix dispatchers.

### 3.3 `ECHOSTUDIO_85_2_Firestick_4K.apk` (`com.athena.display4k`)
* **Chassis Role**: Dedicated 4K television viewport container.
* **Manifest Attributes**: `android.software.leanback` forced `false` (allowing hybrid deployment), banner set to `@drawable/tv_banner`, screen orientation locked to `sensorLandscape`, and keep-screen-on enabled.

### 3.4 `MICHAEL_UK_Smart_Meter.apk` (`com.michael.echoverse`)
* **Chassis Role**: Standalone budget guardian and UK Smart Meter bridge.
* **Operational Goal**: Enforces real-time spend limits and autonomous circuit-breaking for cloud APIs.

### 3.5 Native Hardware Bridges
* **Text-To-Speech (TTS)**: Native Android `android.speech.tts.TextToSpeech` bridge with UK English (`Locale.UK`) voice synthesis piped over Bluetooth audio sinks.
* **Haptics**: Android `Vibrator` and `VibrationEffect` integration providing physical pulse confirmations on pad triggers.

---

## CHAPTER 4: LIVE TELEMETRY, SECURITY & AUTONOMOUS SAFEGUARDS

### 4.1 Dual-Bus Communication Architecture
1. **Local Desktop Bus (`argon_18_telemetry_bus`)**:
   * Uses HTML5 `BroadcastChannel` API across tabs, windows, and pop-outs on the workstation.
   * Delivers strict 0ms latency, zero allocation overhead, and zero packet loss.
2. **Cloud Telemetry Bus (`users/{uid}/telemetry/live_viewport`)**:
   * Backed by Google Cloud Firestore with real-time `onSnapshot` subscriptions.
   * Bridges physical mobile hardware (Moto G15) and remote living room hardware (Fire TV 4K) into the master laptop studio.

### 4.2 Automated Budget Tripwire Daemon (`smartMeterLogger.js`)
* **Baseline**: Tracking starts strictly at £0.00 baseline spend.
* **Soft Tripwire (£160.00)**: UI flashes an amber warning banner across all active viewports and throttles non-essential background worker threads.
* **Hard Circuit Breaker (£200.00)**: Autonomous circuit-breaker trips. All external API generation calls, token ingestion pipelines, and cloud render queries are immediately halted to prevent unexpected billing spikes.

### 4.3 Domain Architecture & Gated Access (`echocreativestudio.store`)
* **Front Door**: Managed WordPress instance on EasyWP/Namecheap acting as the public landing zone.
* **Gated Pathway (`/whizz-kids-warehouse`)**: Password-protected challenge container requesting the developer passphrase (`ECHO-1982`).
* **Frame Ancestors Protection**: Firebase Hosting configured via `firebase.json` headers to permit trusted iframe embedding strictly from `echocreativestudio.store` while forbidding unauthorized clickjacking.

---

## CHAPTER 5: PHYSICAL HARDWARE CONTROLLERS & INPUT MATRIX

### 5.1 GameSir G7 SE Context Routing Engine (`useGamepadController.js`)
* **Hardware Profile**: USB Hall Effect analog sticks with 0% drift, calibrated with a deterministic 12% deadzone clamp in the 60 FPS polling loop.
* **Context 1: `TEXTURE_MAPPER_3D`**:
  * *Left Stick*: Orbital yaw and pitch rotation around the 3D mesh.
  * *Right Stick*: 2D UV texture panning and offset manipulation.
  * *Triggers (LT / RT)*: Smooth analog dolly zoom (camera distance).
  * *Bumpers (LB / RB)*: Instant cycling through target character rig slots (`Cowl` → `Chest_Armor` → `Cape`).
* **Context 2: `READER` (Comic & eBook)**:
  * *D-Pad Left / Right*: Edge-triggered page flipping with haptic feedback.
  * *Right Stick (Y-Axis)*: Smooth continuous reading scroll.
  * *Y Button*: Toggle UI HUD overlay visibility.
* **Context 3: `VIDEOS` / `TIMELINE`**:
  * *Left Stick (X-Axis)*: Variable-speed timeline scrubbing.
  * *A Button*: Video play/pause toggle.
  * *X / B Buttons*: Jump to previous / next scene marker.

### 5.2 Pioneer DDJ-400 Web MIDI Console (`navigator.requestMIDIAccess`)
* **Handshake**: Browser requests raw MIDI stream access on USB connection.
* **Bank Selectors**:
  * `HOT CUE` (Byte `0x1B`): Mounts `Frequency303` acid bassline sequencer.
  * `BEAT LOOP` (Byte `0x1E`): Mounts `Pulse909` analog drum machine grid.
  * `BEAT JUMP` (Byte `0x1F`): Mounts `Solaria106` polyphonic Juno chord bed.
  * `SAMPLER` (Byte `0x20`): Mounts `EchoSampler` for cinematic impacts and warp booms.
* **Pots & Faders (Control Change CC)**:
  * *Filter Knobs (CC 23/24)*: Lowpass/Highpass resonant filter frequency sweep.
  * *EQ Mid (CC 11/12)*: Diode ladder Q and resonance multiplier.
  * *Faders (CC 19/20)*: Channel volume gain staging.
  * *Crossfader (CC 31)*: Seamless A/B transition between audio decks and visual MilkDrop shaders.
* **Audio Routing**: Dedicated split output targeting `LINE (2- DDJ-400)` with 432Hz master tuning bus.

---

## CHAPTER 6: WHIZZ KID'S WAREHOUSE & THE MASTER VAULT (`/vault`)

### 6.1 Warehouse Floor Operations
Located at `/inbox` and `/projects`, the Warehouse functions as the multi-track asset staging deck:
* Dynamic UV wrap workbench for character models and comic book slabs.
* Ingestion staging for narrative comic series (such as *Arthur: Cosmic Odyssey*).
* Zero-loss prompt library and visual template storage.

### 6.2 Master Vault Structure (`/vault`)
Organized across three distinct studio tabs:
* **Tab 1: Batman '89 Video & 3D Wrap Vault**:
  * Houses the 5 master video plates: Crime Alley (`stage_0_alley.mp4`), Gotham Docks (`stage_1_docks.mp4`), Axis Chemicals (`stage_2_axis_chemicals.mp4`), Arkham Gates (`stage_8_arkham_gates.mp4`), and Belfry Climax (`stage_10_climax_belfry.mp4`).
  * Features 1-click 3D Rig Wrapping and live Fire TV telemetry broadcasting.
* **Tab 2: Live Engine Workbench**:
  * Mounts the 5 standalone HTML5 game and audio engines in full-resolution in-studio viewports.
* **Tab 3: APK Packages & Toolkit Registry**:
  * Real-time verification status for all Desktop APK builds and clean engine archives.

---

## CHAPTER 7: 3D MULTI-SLOT UV TEXTURE & VIDEO-WRAPPING PIPELINE

### 7.1 WebGL / Three.js PBR Architecture
* Renders real-time materials using `THREE.MeshStandardMaterial` and `THREE.MeshPhysicalMaterial`.
* Employs isolated sub-mesh targeting on character rigs:
  * **Slot [0]**: Cowl / Head
  * **Slot [1]**: Chest Armor / Torso
  * **Slot [2]**: Cape / Dynamic Cloth Mesh

### 7.2 Dynamic Video Texturing (`THREE.VideoTexture`)
* Connects native HTML5 `<video>` elements directly into the Three.js rendering pipeline via `new THREE.VideoTexture(videoElement)`.
* Updates video textures at 60 FPS without requiring background image sequence extraction or server-side video transcoding.

### 7.3 Aspect Ratio Preservation
* Calculates video dimensions against mesh UV bounds in real time.
* Normalizes `texture.repeat.set(u, v)` and `texture.offset.set(x, y)` to prevent anamorphic stretching across complex curved surfaces.

### 7.4 Capture & Export Pipeline
* **4K Single-Frame Snapshot**: Captures clean WebGL canvas buffers via `canvas.toBlob('image/png')` at native device pixel ratios.
* **60 FPS WebM Turntable Recording**: Utilizes the browser's `MediaRecorder` API directly off `canvas.captureStream(60)` to produce production-grade `.webm` rotation captures with zero dropped frames.

---

## CHAPTER 8: PROPRIETARY GAME ENGINES & TESTBED SUITE

### 8.1 Athena Game Engine (AGE Core) (`athena_game_engine.js`)
* **Vector2D Mathematics**: Continuous position, velocity, and trajectory calculations.
* **Physics & Rigid Bodies**: Continuous collision detection (CCD), elastic restitution, drag friction, and mass-momentum transfer.
* **Particle System**: Multi-emitter physics with lifetime fade, alpha decay, and turbulent drift.
* **Matrix Engine**: Probability evaluation matrices for combat outcomes and athletic xG tactical simulations.
* **Audio Core**: 432Hz harmonic Web Audio synthesizer.

### 8.2 The 4-Module AGE Testbed (`athena_game_engine.html`)
1. **Arena Combat**: Fight Night physics module featuring 4 distinct weight-class inertia profiles and impact restitution.
2. **Pitch Simulation**: Football tactical engine evaluating xG probabilities across real-time player positioning.
3. **Cosmic Velocity**: Zero-gravity Newtonian space navigation with rotational damping and thrust vectors.
4. **Physics Matrix Lab**: Live sandbox for stress-testing gravity coefficients, restitution bounds, and multi-body collisions.

### 8.3 Retro Brawler Integration (`batman_1989_gotham.html`)
* 2D action platformer running on the AGE core.
* Incorporates Dark Deco aesthetic shaders, comic ink cel-shading filters, and full GameSir G7 SE controller support.

### 8.4 3D Holographic Foil Card & WebXR Consoles
* Procedural WebGL chromatic aberration normal-map shaders that shift color sheen based on camera angle and gyroscopic device orientation.
* Complete WebXR device integration for stereoscopic headset displays.

---

## CHAPTER 9: OMNISPACE WORKSPACE & VISUAL STORYBOARDING

### 9.1 Multi-View Architecture
* Integrates `StoryboardView.tsx` (79KB visual sequencer) for end-to-end cinematic planning.
* Links shot composition, script lines, and audio timing into a unified visual strip.

### 9.2 Studio Sub-Suites
* **`ImageStudioView`**: Generative visual asset creation and character turnarounds.
* **`VideoStudioView`**: Video plate sequencing, speed ramping, and loop tuning.
* **`TTSAudioView`**: Spoken dialogue generation, character voice assignment, and audio timing.

### 9.3 Productivity Synchronization
* Cloud synchronization hooks connecting project drafts to Google Drive, Keep, and automated notification hooks.

---

## CHAPTER 10: OPERATOR RUNBOOKS & TROUBLESHOOTING

### 10.1 Daily Studio Cold-Start Procedure
1. Double-click `OPEN_ATHENA_MASTER_VAULT.bat` on the Desktop.
2. Verify browser opens to `http://localhost:5173/vault`.
3. If running dual screens, double-click `LAUNCH_2ND_SCREEN_POPOUT.bat` and drag the frameless window to your secondary display.
4. Connect the GameSir G7 SE gamepad or Pioneer DDJ-400 console; the studio automatically locks onto the hardware.

### 10.2 Fire TV Wireless ADB Deployment Runbook
1. Ensure the Fire TV Stick 4K is connected to the same local Wi-Fi network and note its IP (e.g., `192.168.1.209`).
2. Verify **ADB Debugging** is turned **ON** under **Settings → My Fire TV → Developer Options**.
3. When the workstation issues `adb connect 192.168.1.209:5555`, **look at your TV screen and select "Always allow from this computer"**, then press **OK** on your Fire TV remote.
4. Run `INSTALL_TO_FIRESTICK_OR_PHONE.bat` on the Desktop. The script pushes `ECHOSTUDIO_85_2_Firestick_4K.apk` and launches the 4K display viewport automatically.

### 10.3 Failure Mode Recovery
* **Issue: Fire TV shows "unauthorized" in ADB**:
  * *Resolution*: Turn ADB Debugging OFF and ON again on the Fire TV Stick, then accept the RSA authorization dialog on the television screen.
* **Issue: Local port 5173 busy or locked**:
  * *Resolution*: Open PowerShell and run `Stop-Process -Name node -Force`, then re-run `OPEN_ATHENA_MASTER_VAULT.bat`.
* **Issue: Smart Meter Budget Warning tripped**:
  * *Resolution*: Verify spend metrics inside `smartMeterLogger.js`. Once reviewed, reset baseline spend counter to restore full API dispatch.
