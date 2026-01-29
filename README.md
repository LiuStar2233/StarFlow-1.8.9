# StarFlow Client README

**Last Updated**: 2026/01/29  
**Notice**: Please read this before contributing.  
**Author**: LiuStar2233  
**Main Contributors**: LiuStar2233  
**License**: GPL-3.0

---

## 1. Development Plan

### 1.1 Technology Stack

- Project Base: `MCP919-LWJGL3-Gradle`
- Gradle: `Gradle 9.3 (developBranch) / 8.14 (master)`
- Injection: `SpongePowered Mixin 0.8.7` + `MixinBooter 10.7`
- Packaging: `ShadowJar`
- Runtime JDK: `Azul Zulu JDK 8`
- Compile-time JDK: `Azul Zulu JDK 25 (developBranch) / 21 (master)`

### 1.2 Feature List

**Optimization References/Targets**:
- `Sodium` (rendering optimization)
- `Lithium` (logic optimization)
- `FerrireCore` (memory optimization)
- `Iris` (shader optimization)

#### In Progress
- [ ] `Rewrite sound module (paulscode is spaghetti code)`
- [ ] `Minor bugs in netty components`

#### Planned Features (To Be Implemented)

- [ ] `OverwriteRender`: Rewrite game rendering classes (highest priority; initially implemented via direct source modification, later migrated to Mixin `@Overwrite`)
- [ ] `GUIInGame`: Rewrite in-game GUI classes (high priority; initially via direct source modification, later migrated to Mixin `@Overwrite`)
- [ ] `BackgroundImage`: Custom main menu background (with dedicated GUI manager)
- [ ] `Sprint`: Unconditional forced sprinting (ignoring normal restrictions)
- [ ] `KeepDay`: Force client to always display daytime (does not affect actual game time)
- [ ] `Crosshairs`: Custom crosshair styles and enhancements
- [ ] `FPS`: Frame rate display
- [ ] `CPS`: Clicks-per-second display
- [ ] `Ping`: Network latency display
- [ ] `Reach`: Attack range display
- [ ] `Footprint`: Footstep visualization
- [ ] `BlockOutline`: Highlight block outlines
- [ ] `KeyDisplay`: On-screen key press display
- [ ] `Logo`: Client logo rendering
- [ ] `AutoLight`: Automatic brightness adjustment (optional)
- [ ] `Skin/Capes`: Custom skin and cape import/display (with dedicated GUI)
- [ ] `AutoGG`: Automatically send "GG" upon game completion
- [ ] `JsonConfig`: Configuration storage using JSON
- [ ] `Zoom`: Camera zoom functionality
- [ ] `Freelook`: Free-look camera mode
- [ ] `NoHurtCamera`: Disable camera shake when taking damage
- [ ] `NowTime`: Display local system time
- [ ] `TNTTime`: TNT explosion countdown
- [ ] `EatTime`: Item consumption countdown (optional)
- [ ] `GameTime`: In-game time display (optional)
- [ ] `PotionEffect`: Remaining duration display for potion effects
- [ ] `InventorySort`: Automatic inventory sorting (similar to JEI)
- [ ] `EquipmentDisplay`: Display currently equipped armor
- [ ] `MiniMap`: Mini-map implementation
- [ ] `EasyInventory`: Simplified inventory interactions (e.g., quick block purchasing to specific slots in BedWars)
- [ ] `Animation`: Support for custom animations
- [ ] `UTF-8Input`: UTF-8 character input support
- [ ] `LockEnglish`: Automatically lock input to English during gameplay (except during chat)
- [ ] `VisualMod`: Enhanced visual effects (e.g., particles, shadows, ambient lighting)
- [ ] `AutoTask`: Automated task execution via client commands (based on `ClientCommand`)
- [ ] `AutoTalk`: Auto-send preset messages [doge] (based on `ClientCommand`, may be categorized under `AutoTask`)
- [ ] `InGameRecord/Replay`: In-game recording and replay functionality
- [ ] `GUI-Adaptive`: All GUIs adapt layout based on window size
- [ ] `Music`: In-game background music playback with custom track support (with dedicated GUI)

#### Features Not Planned for Implementation

- `ClientCommand`: Client-side commands require a prefix (e.g., `|sfcc`). Implemented locally via Mixin without server support.
- `TabMod`: Only displays local mod status (e.g., indicator lights in top-left corner); does not modify the server-sent tab list.
- `Scoreboard`: Provided natively by the server; client only enhances visual presentation.
- `Blur`: If retained, must be implemented via shaders (optional).
- `Physical`: Item drop physics simulation; likely abandoned due to high complexity and existing solutions.
- `Multi-Thread`: Multi-threaded execution; high implementation difficulty.
- `NetworkOptimize`: Network optimization (without packet modification); still carries false-positive ban risks.
- `InClash`: Clash proxy support; works out-of-the-box without additional handling.

### 1.3 Current Development Workflow / Stage (None)

- Launch the client using `gradle.build`, specifically the **`gradle task`: `runClient`**.
  1. If successful: Switch to `gradle_optimized.gradle` for development (this uses optimized dependencies but requires source code rewrites).
  2. If failed: Modify `gradle.build` to ensure **`gradle task`: `runClient`** functions correctly.

---

## 2. Feature Classification and Safety Guidelines

All features are categorized into two safety levels:

### ✅ Safe Features

These features **affect only the local client**, do not modify network traffic or simulate player actions, and will not trigger anti-cheat detection:

1. `OverwriteRender`
2. `GUIInGame`
3. `BackgroundImage`
4. `KeepDay`
5. `Crosshairs`
6. `FPS`
7. `CPS`
8. `Ping`
9. `Reach`
10. `Footprint`
11. `BlockOutline`
12. `KeyDisplay`
13. `Logo`
14. `AutoLight`
15. `Skin/Capes`
16. `JsonConfig`
17. `Zoom`
18. `Freelook`
19. `NoHurtCamera`
20. `NowTime`
21. `GameTime`
22. `PotionEffect`
23. `TNTTime`
24. `EatTime`
25. `EquipmentDisplay`
26. `MiniMap`
27. `EasyInventory`
28. `Animation`
29. `UTF-8Input`
30. `LockEnglish`
31. `VisualMod`
32. `Music`
33. `GUI-Adaptive`
34. `TabMod`
35. `Blur`
36. `InClash`

---

### ⚠️ High-Risk Features

These features **may be flagged by anti-cheat systems** and are disabled by default:

1. `Sprint`
2. `AutoGG`
3. `AutoTask`
4. `AutoTalk`
5. `InventorySort`
6. `InGameRecord/Replay`
7. `Physical`
8. `NetworkOptimize`
9. `Multi-Thread`

---

## 3. Development and Release Guidelines

### 3.1 Development Environment Setup

- **Runtime JDK**: Azul Zulu JDK 8
- **Compile-time JDK**: Azul Zulu JDK 25
- **Gradle Version**: Gradle 9.3
- **Mixin Framework**: SpongePowered Mixin 0.8.7 + MixinBooter 10.7
- **Graphics Library**:
  - Phase 1 (may be skipped): Bridge compatibility via `LWJGL3ify`
  - Phase 2: Remove bridge and integrate native LWJGL3 directly
- **Packaging Tool**: ShadowJar

> Note: Development should occur within this environment configuration to ensure compatibility and build consistency.

---

### 3.2 Code Quality and Architecture Principles

- **Fully Modular**: Each feature must be an independent, pluggable module.
- **Implementation Approach**:
  - All features **must be implemented using Mixin**
  - Only two exceptions: `OverwriteRender`, `GUIInGame`, and `Minecraft modules using paulscode` may initially modify source code directly, but **must later be migrated to Mixin `@Overwrite`**. Note: Direct source modification is only for migrating to `LWJGL3` to achieve better performance.
- **Code Style**:
  - Class names: `PascalCase` (e.g., `JsonConfigManager`)
  - Method/variable names: `camelCase` (e.g., `loadConfigFromFile`)
  - Mixin classes: Prefix with `Mixin*` (e.g., `MixinEntityRenderer`)
- **No Spaghetti Code**: Maintain high readability and maintainability; prohibit redundancy, hardcoding, or magic numbers.

---

### 3.3 Configuration System Specification

- **Format**: `Gson` (file handling) + `JSON` (storage format)
- **Path**: `./StarFlow/configs/config.json`
- **Requirements**:
  - Auto-generate with all features disabled by default if missing
  - Option to preserve legacy configurations
  - Support saving modified settings to file and hot-reloading the latest config during gameplay

---

### 3.4 Build and Release Process

- **Build Command**:
  ```bash
  ./gradlew shadowJar
  ```
- **Output Filename Format**:
  ```
  StarFlow-[VERSION]-[YYYYMMDD].jar
  ```
  Example: `StarFlow-v1.1-20260110.jar`
- **Versioning Semantics (MAJOR.PATCH)**:
  - `MAJOR`: New feature added (e.g., `MiniMap`)
  - `PATCH`: Bug fixes or security improvements

> Note: Version increments must strictly correspond to functional changes; skipping numbers or misusing semantics is prohibited.

---

### 3.5 Project Collaboration Guidelines

- **Code Hosting**: GitHub [StarFlow-1.8.9 on GitHub](https://github.com/LiuStar2233/StarFlow-1.8.9)
- **Pull Request Requirements**:
  - Must include **feature description**
  - Must specify **safety classification**
- **Developer Communication**: No official developer group yet; GitHub Issues / PRs are the primary communication channels

---

### 3.6 Open Source Compliance and Licensing

- **License**: GPL-3.0
- **Minecraft EULA Compliance**:
  > "This mod is your original work and does not contain substantial portions of Mojang's code or content… The mod may be freely distributed, but distribution of a complete Minecraft version bundled with this mod is not permitted."

---

## 4. Open Source Dependencies and Acknowledgements

- [MCP919](https://github.com/Marcelektro/MCP-919): For decompiled Minecraft 1.8.9 source code and resources
- [MCP919-LWJGL3-Gradle](https://github.com/RATMC/MCP919-LWJGL3-Gradle): Base build environment and integration
- [SpongePowered Mixin](https://github.com/GTNewHorizons/SpongePoweredMixin): Injection framework
- [MixinBooter](https://github.com/CleanroomMC/MixinBooter): Injection loader
- [FPSMaster](https://github.com/SuperSkidder/FPSMaster): Feature reference
- [LiuStar2233](https://github.com/LiuStar2233): Core technical contributor
- [ULAchelous](https://github.com/ULAchelous): Core technical contributor
- Our testers
- All open source dependencies used in this project

---