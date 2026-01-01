# StarFlow Client README  

**Last Updated**: 2026/1/1  
**Note**: This document serves as a development guide.  
**Author**: LiuStar2233  

---

## 1. Development Plan

### 1.1 Technology Stack
We are developing the client using:  
`MCP919 + LWJGL3 + SpongePowered Mixin + ShadowJar` and `Azul Zulu JDK 8`.

### 1.2 Feature List

**Optimization References / Targets**:  
- `Sodium` (rendering optimization)  
- `Lithium` (game logic optimization)  
- `FerrireCore` (memory optimization)  
- `Iris` (shader optimization)

**Planned Features (To Be Implemented)**  
- [ ] `OverwriteRender`: Rewrite Minecraft’s rendering classes.  
  *(Implemented first; initially via direct source modification, later migrated to Mixin’s `@Overwrite`.)*  
- [ ] `GUIInGame`: Rewrite in-game GUI classes.  
  *(Implemented early; initially via direct source modification, later migrated to Mixin’s `@Overwrite`.)*  
- [ ] `BackgroundImage`: Custom main menu background (with dedicated GUI manager)  
- [ ] `Sprint`: Unconditional forced sprint (ignoring normal restrictions)  
- [ ] `KeepDay`: Force client to always display daytime (does not affect actual in-game time)  
- [ ] `Crosshairs`: Customizable crosshair styles and enhancements  
- [ ] `FPS`: Display frames per second  
- [ ] `CPS`: Display clicks per second  
- [ ] `Ping`: Display network latency  
- [ ] `Reach`: Display attack reach distance  
- [ ] `Footprint`: Visualize player footprints  
- [ ] `BlockOutline`: Highlight block outlines  
- [ ] `KeyDisplay`: On-screen key press display  
- [ ] `Logo`: Render client logo  
- [ ] `AutoLight`: Automatic brightness adjustment (optional)  
- [ ] `Skin/Capes`: Import and display custom skins and capes (with dedicated GUI)  
- [ ] `AutoGG`: Automatically send "GG" when a game ends  
- [ ] `JsonConfig`: Store configuration using JSON  
- [ ] `Zoom`: Camera zoom functionality  
- [ ] `Freelook`: Free-look camera mode  
- [ ] `NoHurtCamera`: Disable camera shake when taking damage  
- [ ] `NowTime`: Display local system time  
- [ ] `TNTTime`: Countdown timer for TNT explosions  
- [ ] `EatTime`: Countdown timer for item consumption (optional)  
- [ ] `GameTime`: Display in-game time (optional)  
- [ ] `PotionEffect`: Display remaining duration of potion effects  
- [ ] `InventorySort`: Auto-sort inventory (similar to JEI)  
- [ ] `EquipmentDisplay`: Display currently equipped armor  
- [ ] `MiniMap`: Mini-map implementation  
- [ ] `EasyInventory`: Simplified inventory interaction (e.g., quick purchase to specific slots in BedWars)  
- [ ] `Animation`: Support for custom animations  
- [ ] `UTF-8Input`: Support UTF-8 character input  
- [ ] `LockEnglish`: Automatically lock keyboard input to English during gameplay (except during free typing)  
- [ ] `VisualMod`: Enhanced visual effects (e.g., particles, shadows, ambient lighting)  
- [ ] `AutoTask`: Automatically execute client-side tasks (based on `ClientCommand`)  
- [ ] `AutoTalk`: Automatically send pre-defined messages (based on `ClientCommand`; can be grouped under `AutoTask`)  
- [ ] `InGameRecord/Replay`: In-game recording and replay functionality  
- [ ] `GUI-Adaptive`: All GUIs adapt layout based on window size  
- [ ] `Music`: In-game background music player with support for custom tracks (with dedicated GUI)

**Features Not Planned for Implementation**  
- `ClientCommand`: Client-side commands require a prefix (e.g., `|sfcc`). Implemented locally via Mixin, no server support needed.  
- `TabMod`: Only displays local mod status (e.g., a status indicator in the top-left corner), does not modify the server-sent tab list.  
- `Scoreboard`: Provided natively by the server; client only enhances visual appearance.  
- `Blur`: If retained, must be implemented via shaders (optional).  
- `Physical`: Item-drop physics simulation; likely abandoned due to high complexity and existing solutions.  
- `Multi-Thread`: Multi-threaded execution; high implementation difficulty.  
- `NetworkOptimize`: Network optimization (without modifying packets), but still carries risk of false bans.  
- `InClash`: Clash proxy support; works out-of-the-box, no extra implementation needed.

### 1.3 Current Development Workflow / Stage
- Use `gradle.build` as the base build file (contains vanilla dependencies). Attempt to launch the client via the Gradle task: `runClient`.  
- **If successful**: Replace with `gradle.build.1` (our optimized dependency configuration) for development. This requires rewriting core classes such as `OverwriteRender`, `GUIInGame`, etc.  
- **If failed**: Modify `gradle.build` until `runClient` successfully launches the client.

---

## 2. Feature Classification & Safety Guidelines

All features are classified into two categories based on safety:

### ✅ Safe Features  
These features **only affect the local client**—they **do not modify network communication** or **simulate player actions**, and **will not trigger anti-cheat detection**:

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
These features **may be flagged by anti-cheat systems** as violations, **default disabled**, and **require explicit user opt-in**:

- `Sprint`  
- `AutoGG`  
- `AutoTask`  
- `AutoTalk`  
- `InventorySort`  
- `InGameRecord/Replay`  
- `Physical`  
- `NetworkOptimize`  
- `Multi-Thread`

> **Policy**: All high-risk features are **disabled by default**. Users must **explicitly enable them via config**, and the UI **must display a prominent “High Risk” warning** when enabled.

---

## 3. Development & Release Guidelines

### 3.1 Development Environment Setup
- **JDK**: Azul Zulu JDK 8 (preview or higher versions prohibited)  
- **Mixin Framework**: SpongePowered Mixin 0.7.11  
- **Graphics Library**:  
  - **Phase 1**: Use `LWJGL3ify` bridge for LWJGL3 compatibility  
  - **Phase 2**: Remove bridge and integrate native LWJGL3 directly  
- **Packaging Tool**: ShadowJar  

> All development must occur in this environment to ensure compatibility and build consistency.

### 3.2 Code Quality & Architecture Principles
- **Modularity**: Achieve **100% modular design**—each feature must be an independent, pluggable module.  
- **Implementation**:  
  - All features **must be implemented via Mixin**  
  - **Only exceptions**: `OverwriteRender` and `GUIInGame` may initially use direct source edits, but must be migrated to Mixin’s `@Overwrite` later  
- **Code Style**:  
  - Class names: `PascalCase` (e.g., `JsonConfigManager`)  
  - Method/variable names: `camelCase` (e.g., `loadConfigFromFile`)  
  - Mixin classes: Prefix with `Mixin*` (e.g., `MixinEntityRenderer`)  
- **No junk code**: Maintain high readability and maintainability; prohibit redundancy, hardcoded values, or magic numbers.

### 3.3 Configuration System Specification
- **Format**: `Gson + JSON`  
- **Path**: `./starflow/config.json`  
- **Requirements**:  
  - Support **hot-reloading** (changes apply without game restart)  
  - All configurable options must have **sensible defaults**  
  - Config file must be **auto-generated on first launch**

### 3.4 Build & Release Process
- **Build Command**:  
  ```bash
  ./gradlew shadowJar
  ```
- **Output Filename Format**:  
  `StarFlow-[VERSION]-[YYYYMMDD].jar`  
  Example: `StarFlow-v2.1.0-20251227.jar`  
- **Versioning Semantics (MAJOR.PATCH)**:  
  - `MAJOR`: New feature added (e.g., `MiniMap`)  
  - `PATCH`: Bug fix or security improvement  
> Version numbers must strictly reflect actual changes—no skipping or semantic abuse.

### 3.5 Project Collaboration Guidelines
- **Code Hosting**: GitHub ([StarFlow-1.8.9](https://github.com/LiuStar2233/StarFlow-1.8.9))  
- **Pull Request Requirements**:  
  - Must include a **feature description**  
  - Must specify **safety classification** (✅ Safe / ⚠️ High-Risk)  
- **Developer Communication**: No official group yet; use GitHub Issues / PRs as the primary channel.

### 3.6 Open-Source Compliance & Licensing
- **License**: GPL-3.0  
- **Compliance with Minecraft EULA**:  
  > “This mod is your original work and does not contain substantial portions of Mojang’s code or content… The mod may be freely distributed, but you may not distribute a complete version of Minecraft bundled with this mod.”

All contributors must ensure their code respects Mojang's copyright and complies with the open-source license.

---

## 4. Open-Source Dependencies & Acknowledgements

- [**MCP919**](https://github.com/Marcelektro/MCP-919): Provides decompiled Minecraft 1.8.9 source code and resources  
- [**MCP919-LWJGL3-Gradle**](https://github.com/Eatgrapes/MCP919-LWJGL3-Gradle): Base build environment and integration reference  
- [**SpongePowered Mixin**](https://github.com/GTNewHorizons/SpongePoweredMixin): Core bytecode injection framework  
- [**FPSMaster**](https://github.com/WithoutIQ/FPSMaster-v2): Reference implementation for rendering and performance optimization  
- [**ULAchelous**](https://github.com/ULAchelous): Core team developer  
- [**LiuStar2233**](https://github.com/LiuStar2233): Core team developer  
- And our testers

--- 