# StarFlow Client README  

**Last Updated:** 2026/01/10  
**Note:** This document serves as a development guide. All contributors must adhere to its specifications.  
**Author:** LiuStar2233

---

## 1. Development Plan

### 1.1 Technology Stack  
We develop the client using:  
- **MCP919**  
- **LWJGL3**  
- **SpongePowered Mixin**  
- **ShadowJar**  
- **Azul Zulu JDK 8**

### 1.2 Feature List  

**Optimization References / Targets:**  
- `Sodium` (rendering optimization)  
- `Lithium` (game logic optimization)  
- `FerrireCore` (memory optimization)  
- `Iris` (shader optimization)

**Planned Features (To Be Implemented):**  
- [ ] `OverwriteRender`: Rewrite game rendering classes (highest priority; initially implemented via direct source modification, later migrated to Mixin’s `@Overwrite`)  
- [ ] `GUIInGame`: Rewrite in-game GUI classes (high priority; same implementation strategy as above)  
- [ ] `BackgroundImage`: Custom main menu background (with dedicated GUI manager)  
- [ ] `Sprint`: Force sprint unconditionally (ignoring normal restrictions)  
- [ ] `KeepDay`: Force client to always display daytime (does not affect actual game time)  
- [ ] `Crosshairs`: Custom crosshair style and enhancements  
- [ ] `FPS`: Display frames per second  
- [ ] `CPS`: Display clicks per second  
- [ ] `Ping`: Display network latency  
- [ ] `Reach`: Display attack reach distance  
- [ ] `Footprint`: Visualize player footprints  
- [ ] `BlockOutline`: Highlight block outlines  
- [ ] `KeyDisplay`: On-screen key press indicators  
- [ ] `Logo`: Render client logo  
- [ ] `AutoLight`: Automatic brightness adjustment (optional)  
- [ ] `Skin/Capes`: Import and display custom skins and capes (with dedicated GUI)  
- [ ] `AutoGG`: Automatically send "GG" at game end  
- [ ] `JsonConfig`: Store configuration in JSON format  
- [ ] `Zoom`: Camera zoom functionality  
- [ ] `Freelook`: Free-look camera mode  
- [ ] `NoHurtCamera`: Disable camera shake when taking damage  
- [ ] `NowTime`: Display local system time  
- [ ] `TNTTime`: TNT explosion countdown  
- [ ] `EatTime`: Item consumption countdown (optional)  
- [ ] `GameTime`: Display in-game time (optional)  
- [ ] `PotionEffect`: Show remaining duration of potion effects  
- [ ] `InventorySort`: Auto-sort inventory (similar to JEI)  
- [ ] `EquipmentDisplay`: Show currently equipped armor  
- [ ] `MiniMap`: Implement minimap  
- [ ] `EasyInventory`: Simplify inventory interactions (e.g., quick-buy blocks into specific slots in BedWars)  
- [ ] `Animation`: Support custom animations  
- [ ] `UTF-8Input`: Enable UTF-8 character input  
- [ ] `LockEnglish`: Automatically lock input to English during gameplay (except during chat typing)  
- [ ] `VisualMod`: Enhanced visual effects (e.g., particles, shadows, ambient lighting)  
- [ ] `AutoTask`: Execute automated tasks via client commands (`ClientCommand`)  
- [ ] `AutoTalk`: Send preset messages automatically (based on `ClientCommand`, can be grouped under `AutoTask`)  
- [ ] `InGameRecord/Replay`: In-game recording and replay  
- [ ] `GUI-Adaptive`: All GUIs adapt layout based on window size  
- [ ] `Music`: Play background music with support for custom tracks (with dedicated GUI)

**Features Not Planned for Implementation:**  
- `ClientCommand`: Client-side commands must use a prefix (e.g., `|sfcc`). Implemented locally via Mixin without server support.  
- `TabMod`: Only displays local mod status (e.g., indicator light in top-left); does not modify the server-sent tab list.  
- `Scoreboard`: Provided natively by the server; client only enhances visual appearance.  
- `Blur`: If retained, must be implemented via shaders (optional).  
- `Physical`: Item drop physics simulation—likely abandoned due to high complexity and existing solutions.  
- `Multi-Thread`: Multi-threaded execution—too difficult to implement reliably.  
- `NetworkOptimize`: Network optimization (without modifying packets)—still carries ban risk.  
- `InClash`: Clash proxy support—already works out-of-the-box; no extra handling needed.

---

## 2. Feature Classification & Safety Guidelines

All features are categorized by safety:

✅ **Safe Features**  
These affect only the local client, do not alter network communication or simulate player actions, and will not trigger anti-cheat detection:  
`OverwriteRender`, `GUIInGame`, `BackgroundImage`, `KeepDay`, `Crosshairs`, `FPS`, `CPS`, `Ping`, `Reach`, `Footprint`, `BlockOutline`, `KeyDisplay`, `Logo`, `AutoLight`, `Skin/Capes`, `JsonConfig`, `Zoom`, `Freelook`, `NoHurtCamera`, `NowTime`, `GameTime`, `PotionEffect`, `TNTTime`, `EatTime`, `EquipmentDisplay`, `MiniMap`, `EasyInventory`, `Animation`, `UTF-8Input`, `LockEnglish`, `VisualMod`, `Music`, `GUI-Adaptive`, `TabMod`, `Blur`, `InClash`

⚠️ **High-Risk Features**  
These may be flagged by anti-cheat systems and are disabled by default:  
`Sprint`, `AutoGG`, `AutoTask`, `AutoTalk`, `InventorySort`, `InGameRecord/Replay`, `Physical`, `NetworkOptimize`, `Multi-Thread`

---

## 3. Development & Release Standards

### 3.1 Development Environment Setup  
- **JDK**: Azul Zulu JDK 8 (preview or newer versions strictly prohibited)  
- **Mixin Framework**: SpongePowered Mixin 0.7.11  
- **Graphics Library**:  
  - **Phase 1** (may be skipped): Use `LWJGL3ify` as a bridge for LWJGL3 compatibility  
  - **Phase 2**: Remove bridge and integrate native LWJGL3 directly  
- **Packaging Tool**: ShadowJar  
> All development must occur under this exact environment to ensure compatibility and build consistency.

### 3.2 Code Quality & Architecture Principles  
- **Modularity**: Fully modular design—each feature must be an independent, pluggable module.  
- **Implementation**:  
  - All features **must** be implemented using Mixin.  
  - **Only exceptions**: `OverwriteRender` and `GUIInGame` may initially modify source code directly, but must later be migrated to Mixin’s `@Overwrite`.  
- **Code Style**:  
  - Class names: `PascalCase` (e.g., `JsonConfigManager`)  
  - Method/variable names: `camelCase` (e.g., `loadConfigFromFile`)  
  - Mixin classes: Prefix with `Mixin*` (e.g., `MixinEntityRenderer`)  
- **Code Quality**: No "spaghetti code." Maintain high readability and maintainability. Avoid redundancy, hardcoding, and magic numbers.

### 3.3 Configuration System Specification  
- **Format**: `Gson` (for file handling) + `JSON` (storage format)  
- **Path**: `./StarFlow/configs/config.json`  
- **Requirements**:  
  - If config file doesn’t exist, generate with all features **disabled by default**.  
  - Support preserving old configs if desired.  
  - Allow in-game reloading of the latest config after modification.

### 3.4 Build & Release Process  
- **Build Command**:  
  ```bash
  ./gradlew shadowJar
  ```
- **Output Filename Format**:  
  `StarFlow-[VERSION]-[YYYYMMDD].jar`  
  Example: `StarFlow-v1.1-20260110.jar`  
- **Versioning Semantics (MAJOR.PATCH)**:  
  - `MAJOR`: New feature added (e.g., `MiniMap`)  
  - `PATCH`: Bug fixes or security improvements  
> Version numbers must strictly reflect changes—no skipping or semantic abuse.

### 3.5 Project Collaboration Guidelines  
- **Code Hosting**: GitHub — [StarFlow-1.8.9](https://github.com/LiuStar2233/StarFlow-1.8.9)  
- **Pull Request Requirements**:  
  - Must include a feature description  
  - Must specify safety classification (✅ Safe / ⚠️ High-Risk)  
- **Developer Communication**: No official group yet; use GitHub Issues and PRs as primary channels.

### 3.6 Open Source Compliance & License  
- **License**: GPL-3.0  
- **Minecraft EULA Compliance**:  
  > “This mod is your original work and does not contain substantial portions of Mojang’s code or content… The mod may be freely distributed, but you may not distribute a complete version of Minecraft bundled with this mod.”

---

## 4. Dependencies & Acknowledgements  

- [MCP919](https://github.com/Marcelektro/MCP-919): Provides decompiled Minecraft 1.8.9 source and resources  
- [MCP919-LWJGL3-Gradle](https://github.com/Eatgrapes/MCP919-LWJGL3-Gradle): Base build environment and integration reference  
- [SpongePowered Mixin](https://github.com/GTNewHorizons/SpongePoweredMixin): Core bytecode injection framework  
- [FPSMaster](https://github.com/SuperSkidder/FPSMaster): Reference for rendering and performance optimization  
- [LiuStar2233](https://github.com/LiuStar2233): Core team member  
- [ULAchelous](https://github.com/ULAchelous): Core team member  
- And our testers

--- 
