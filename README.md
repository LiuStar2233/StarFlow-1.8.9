# StarFlow Client README

**Last Updated:** 2026/01/25  
**Note:** Please read this before contributing.  
**Author:** LiuStar2233  
**Main Contributors:** LiuStar2233  
**License:** GPL-3.0

---

## 1. Development Plan

### 1.1 Technology Stack

- **Base:** `MCP919-LWJGL3-Gradle`
- **Injection Framework:** `SpongePowered Mixin`
- **Packaging Tool:** `ShadowJar`
- **Runtime JDK:** `Azul Zulu JDK 8`
- **Compile-time JDK:** `Azul Zulu JDK 25`

### 1.2 Feature List

#### Optimization References / Goals:
- `Sodium` (rendering optimization)
- `Lithium` (logic optimization)
- `FerrireCore` (memory optimization)
- `Iris` (shader optimization)

#### Planned Features (To Be Implemented):
- [ ] `OverwriteRender`: Rewrite Minecraft’s rendering classes (highest priority; initially implemented via direct source modification, later migrated to Mixin `@Overwrite`)
- [ ] `GUIInGame`: Rewrite in-game GUI classes (high priority; same implementation strategy as above)
- [ ] `BackgroundImage`: Custom main menu background (with dedicated GUI manager)
- [ ] `Sprint`: Force sprint unconditionally (ignoring normal restrictions)
- [ ] `KeepDay`: Force client to always display daytime (does not affect actual game time)
- [ ] `Crosshairs`: Custom crosshair styles and enhancements
- [ ] `FPS`: Display frames per second
- [ ] `CPS`: Display clicks per second
- [ ] `Ping`: Display network latency
- [ ] `Reach`: Display attack reach distance
- [ ] `Footprint`: Visualize player footprints
- [ ] `BlockOutline`: Highlight block outlines
- [ ] `KeyDisplay`: On-screen key press indicators
- [ ] `Logo`: Render client logo
- [ ] `AutoLight`: Auto brightness adjustment (optional)
- [ ] `Skin/Capes`: Import and display custom skins and capes (with dedicated GUI)
- [ ] `AutoGG`: Automatically send “GG” when a match ends
- [ ] `JsonConfig`: Store configuration using JSON
- [ ] `Zoom`: Camera zoom functionality
- [ ] `Freelook`: Free-look camera mode
- [ ] `NoHurtCamera`: Disable camera shake when taking damage
- [ ] `NowTime`: Display local system time
- [ ] `TNTTime`: TNT explosion countdown timer
- [ ] `EatTime`: Item consumption countdown (optional)
- [ ] `GameTime`: Display in-game time (optional)
- [ ] `PotionEffect`: Show remaining duration of potion effects
- [ ] `InventorySort`: Auto-sort inventory (similar to JEI)
- [ ] `EquipmentDisplay`: Display currently equipped armor
- [ ] `MiniMap`: Implement minimap
- [ ] `EasyInventory`: Simplify inventory interactions (e.g., quick-buy blocks into specific slots in BedWars)
- [ ] `Animation`: Support custom animations
- [ ] `UTF-8Input`: Enable UTF-8 character input
- [ ] `LockEnglish`: Auto-lock keyboard input to English during gameplay (except during chat typing)
- [ ] `VisualMod`: Enhanced visual effects (e.g., particles, shadows, ambient lighting)
- [ ] `AutoTask`: Execute automated tasks via client commands (`ClientCommand`)
- [ ] `AutoTalk`: Send preset messages automatically (based on `ClientCommand`, can be grouped under `AutoTask`)
- [ ] `InGameRecord/Replay`: In-game recording and replay
- [ ] `GUI-Adaptive`: All GUIs adapt layout based on window size
- [ ] `Music`: Play background music in-game with support for custom tracks (with dedicated GUI)

#### Features Not Planned for Implementation:
- `ClientCommand`: Client-side commands must use a prefix (e.g., `|sfcc`). Implemented locally via Mixin, no server support needed.
- `TabMod`: Only displays local mod status (e.g., top-left indicator), does not modify the server-sent tab list.
- `Scoreboard`: Provided natively by the server; client only enhances visual appearance.
- `Blur`: If retained, must be implemented via shaders (optional).
- `Physical`: Item drop physics simulation — high complexity and existing solutions available; likely abandoned.
- `Multi-Thread`: Multi-threaded execution — high implementation difficulty.
- `NetworkOptimize`: Network optimization without modifying packets — still carries anti-cheat detection risk.
- `InClash`: Clash proxy support — works out-of-the-box; no extra handling needed.

### 1.3 Current Development Workflow / Stage (None)

- Use `gradle.build` to launch the client via the Gradle task: `runClient`.
- **If successful**: Switch to `gradle_optimized.gradle` for development (this uses optimized dependencies but requires rewriting source code).
- **If failed**: Modify `gradle.build` to ensure `runClient` works properly.

---

## 2. Feature Classification & Safety Guidelines

All features are categorized into two safety levels:

### ✅ Safe Features
These only affect the local client, do not alter network communication or simulate player actions, and will **not** trigger anti-cheat systems:

- `OverwriteRender`
- `GUIInGame`
- `BackgroundImage`
- `KeepDay`
- `Crosshairs`
- `FPS`
- `CPS`
- `Ping`
- `Reach`
- `Footprint`
- `BlockOutline`
- `KeyDisplay`
- `Logo`
- `AutoLight`
- `Skin/Capes`
- `JsonConfig`
- `Zoom`
- `Freelook`
- `NoHurtCamera`
- `NowTime`
- `GameTime`
- `PotionEffect`
- `TNTTime`
- `EatTime`
- `EquipmentDisplay`
- `MiniMap`
- `EasyInventory`
- `Animation`
- `UTF-8Input`
- `LockEnglish`
- `VisualMod`
- `Music`
- `GUI-Adaptive`
- `TabMod`
- `Blur`
- `InClash`

### ⚠️ High-Risk Features
These may be flagged by anti-cheat systems and are **disabled by default**:

- `Sprint`
- `AutoGG`
- `AutoTask`
- `AutoTalk`
- `InventorySort`
- `InGameRecord/Replay`
- `Physical`
- `NetworkOptimize`
- `Multi-Thread`

---

## 3. Development & Release Standards

### 3.1 Development Environment Setup
- **Runtime JDK:** Azul Zulu JDK 8
- **Compile-time JDK:** Azul Zulu JDK 25
- **Gradle Version:** Gradle 9.3
- **Mixin Framework:** SpongePowered Mixin 0.7.11
- **Graphics Library:**
  - **Phase 1 (possibly skipped):** Use `LWJGL3ify` bridge for LWJGL3 compatibility
  - **Phase 2:** Remove bridge and integrate native LWJGL3 directly
- **Packaging Tool:** ShadowJar
> *Note: Development must occur under this exact environment configuration to ensure compatibility and build consistency.*

### 3.2 Code Quality & Architecture Principles
- **Fully Modular:** Each feature must be an independent, pluggable module.
- **Implementation Approach:**
  - All features **must** be implemented using Mixin.
  - **Exceptions:** `OverwriteRender`, `GUIInGame`, and modules using `paulscode` in Minecraft may initially modify source code directly, but **must** be migrated to Mixin `@Overwrite` later.
    > *Rationale: Direct source modification is only for initial LWJGL3 migration to gain performance benefits.*
- **Code Style:**
  - Class names: `PascalCase` (e.g., `JsonConfigManager`)
  - Method/variable names: `camelCase` (e.g., `loadConfigFromFile`)
  - Mixin classes: Prefix with `Mixin*` (e.g., `MixinEntityRenderer`)
- **No "Spaghetti Code":** Maintain high readability and maintainability. Avoid redundancy, hard-coded values, or magic numbers.

### 3.3 Configuration System Specification
- **Format:** `Gson` (for file handling) + `JSON` (storage format)
- **Path:** `./StarFlow/configs/config.json`
- **Requirements:**
  - If config file doesn’t exist, auto-generate with **all features disabled by default**.
  - Option to preserve old config on update.
  - Support runtime reloading of the latest config after modification.

### 3.4 Build & Release Process
- **Build Command:**
  ```bash
  ./gradlew shadowJar
  ```
- **Output Filename Format:**  
  `StarFlow-[VERSION]-[YYYYMMDD].jar`  
  Example: `StarFlow-v1.1-20260110.jar`
- **Versioning Semantics (MAJOR.PATCH):**
  - `MAJOR`: New feature added (e.g., `MiniMap`)
  - `PATCH`: Bug fixes or security improvements
  > *Note: Version numbers must strictly reflect changes. No skipping or semantic abuse.*

### 3.5 Collaboration Guidelines
- **Repository:** [StarFlow-1.8.9 on GitHub](https://github.com/LiuStar2233/StarFlow-1.8.9)
- **Pull Request Requirements:**
  - Must include feature description
  - Must specify safety classification (Safe / High-Risk)
- **Communication:** No official dev group yet; use GitHub Issues / PRs as primary channels.

### 3.6 Open Source Compliance & License
- **License:** GPL-3.0
- **Minecraft EULA Compliance:**
  > “This mod is your original work and does not contain substantial portions of Mojang’s code or content… The mod may be freely distributed, but you may not distribute a complete version of Minecraft bundled with this mod.”

---

## 4. Open Source Dependencies & Acknowledgements

- [MCP919](https://github.com/Marcelektro/MCP-919): For decompiled Minecraft 1.8.9 source and resources
- [MCP919-LWJGL3-Gradle](https://github.com/RATMC/MCP919-LWJGL3-Gradle): Base build environment and integration
- [SpongePowered Mixin](https://github.com/GTNewHorizons/SpongePoweredMixin): Injection framework
- [FPSMaster](https://github.com/SuperSkidder/FPSMaster): Feature reference
- [LiuStar2233](https://github.com/LiuStar2233): Core technical contributor
- [ULAchelous](https://github.com/ULAchelous): Core technical contributor
- Our testers
- Other open-source dependencies used in the project

---