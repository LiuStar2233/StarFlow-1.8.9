# StarFlow Client README

- **Version**: v2.3.1
- **Last Updated**: 2025-12-28
- **Description**: This document serves as the development guideline.
- **Author**: LiuStar2233

---

## 1. Development Plan

### 1.1 Development Tech Stack

- We use `MCP919 + LWJGL3 + SpongePowered Mixin + ShadowJar` and `Azul Zulu JDK 8` to develop the client.

### 1.2 Features

- **Optimization Reference/Goal**: **`Sodium`** (Rendering Optimization), **`Lithium`** (Logical Optimization), *
  *`FerrireCore`** (Memory Optimization), **`Iris`** (Shader Optimization).
- [ ] `OverwriteRender`: Rewrite game rendering classes (highest priority; initially implemented via direct source
  modification, later migrated to Mixin `@Overwrite`).
- [ ] `GUIInGame`: Rewrite in-game GUI classes.
- [ ] `BackgroundImage`: Custom main menu background (with dedicated GUI manager).
- [ ] `Sprint`: Force sprinting anywhere without interruption.
- [ ] `KeepDay`: Force client-side daylight rendering (does not affect actual game time).
- [ ] `Crosshairs`: Crosshair customization and enhancement.
- [ ] `FPS`: Frame rate display.
- [ ] `CPS`: Clicks-per-second display.
- [ ] `Ping`: Network latency display.
- [ ] `Reach`: Attack reach display.
- [ ] `Footprint`: Footstep visualization.
- [ ] `BlockOutline`: Highlight block outlines.
- [ ] `KeyDisplay`: On-screen key press display.
- [ ] `Logo`: Client logo rendering.
- [ ] `AutoLight`: Automatic brightness adjustment (via in-game slider).
- [ ] `Skin/Capes`: Custom skin and cape import/display (dedicated GUI).
- [ ] `AutoGG`: Automatically send “GG” upon game end.
- [ ] `JsonConfig`: Use JSON for configuration storage.
- [ ] `Zoom`: Camera zoom functionality.
- [ ] `Freelook`: Free-look camera mode.
- [ ] `NoHurtCamera`: Disable camera shake on damage.
- [ ] `NowTime`: Display local system time.
- [ ] `TNTTime`: TNT explosion countdown.
- [ ] `EatTime`: Item consumption countdown (optional).
- [ ] `GameTime`: In-game time display (optional).
- [ ] `PotionEffect`: Potion effect duration display.
- [ ] `InventorySort`: Inventory sorting (similar to JRE).
- [ ] `EquipmentDisplay`: Show currently equipped armor.
- [ ] `MiniMap`: Minimap implementation.
- [ ] `EasyInventory`: Simplified inventory interaction (e.g., quick-buy blocks in BedWars to specific slots).
- [ ] `Animation`: Custom animation support.
- [ ] `ChineseInput`: Chinese input method support.
- [ ] `LockEnglish`: Automatically lock input to English during gameplay (except during active typing).
- [ ] `VisualMod`: Enhanced visual effects (e.g., particles, shadows, ambient lighting).
- [ ] `AutoTask`: Automated task execution via client-side commands.
- [ ] `AutoFucking`: Auto-send preset offensive messages [doge] (command-based).
- [ ] `InGameRecord/Replay`: In-game video recording and replay.
- [ ] `GUI-Adaptive`: Adaptive layout for all GUIs based on window size.
- [ ] `Music`: In-game background music playback with custom track support (dedicated GUI).

#### Features Not Currently Planned for Implementation

- **`Command`**: Client-side commands require a prefix (e.g., `|sfcc`). Implemented locally via Mixin without server
  dependency.
- **`TabMod`**: Only displays local mod status (e.g., top-left indicator); does not alter server-sent tab data.
- **`Scoreboard`**: Provided natively by the server; client only enhances visual presentation.
- **`Blur`**: If retained, must be implemented via shaders (optional).
- **`Physical`**: Item drop physics simulation; likely removed due to complexity and existing solutions.
- **`Multi-Thread`**: Multi-threaded execution; high implementation difficulty.
- **`NetworkOptimize`**: Network optimization (no packet modification), but still risks false bans.
- **`InClash`**: Clash proxy support; already compatible out-of-the-box, no extra handling needed.

---

## 2. Functional Requirements Classification and Safety Guidelines

All features are categorized into two security levels:

### ✅ Safe

> Only affects local rendering, UI, configuration, or input. **Does not send unconventional network packets**, **does
not simulate player behavior**, and **will not be flagged by anti-cheat systems**.

- `OverwriteRender`
- `GUIInGame`
- `BackgroundImage`
- `KeepDay`
- `Crosshairs`
- `FPS` / `CPS` / `Ping` / `Reach`
- `Footprint` / `BlockOutline`
- `KeyDisplay`
- `Logo`
- `AutoLight`
- `Skin/Capes`
- `JsonConfig`
- `Zoom` / `Freelook`
- `NoHurtCamera`
- `NowTime` / `GameTime` / `PotionEffect` / `TNTTime` / `EatTime`
- `EquipmentDisplay`
- `MiniMap` (based on local chunks only)
- `EasyInventory` (UI preview only, no auto-clicking)
- `Animation`
- `ChineseInput` / `LockEnglish`
- `VisualMod`
- `Music`
- `GUI-Adaptive`
- `TabMod` (local display only)
- `Blur` (shader-based)
- `InClash`

### ⚠️ Risky

> May be falsely flagged by anti-cheat systems, violate server rules, or involve sensitive behavior.

- `Sprint` (abnormal movement)
- `AutoGG` (auto-chat)
- `AutoTask` (auto-command execution)
- `AutoFucking` (auto-toxic messages)
- `InventorySort` (if implemented with auto-clicking)
- `InGameRecord/Replay` (frame/memory capture may trigger detection)
- `Physical` (predictive behavior)
- `NetworkOptimize` (unusual connection patterns)
- `Multi-Thread` (may break anti-cheat sandbox)

> **Policy**: All high-risk features are **disabled by default** and must be explicitly enabled via configuration. A
> “high-risk” warning must be displayed in the UI when activated.

---

## 3. Development and Release Standards

### 3.1 Environment

- **JDK**: Azul Zulu JDK 8 (**no preview versions**)
- **Mixin**: `SpongePowered Mixin 0.7.11`
- **LWJGL**: Initially bridged via `LWJGL3ify`, later directly replaced with `LWJGL3`
- **Packaging**: `ShadowJar`

### 3.2 Build and Release

#### Build Command

```bash
./gradlew shadowJar
```

#### Output Filename Format

```
StarFlow-[VERSION]-[YYYYMMDD].jar
```

Example: `StarFlow-v2.1.0-20251227.jar`

#### Versioning Semantics (`MAJOR.PATCH`)

- **MAJOR**: New feature (e.g., `MiniMap`)
- **PATCH**: Bug fix or safety optimization

### 3.3 Code Quality and Architectural Principles

- **No Shit Code**
- **100% Modular Design**
- **100% Mixin-Based Implementation** (except `OverwriteRender` and `GUIInGame`)
- **LWJGL Migration Path**:
    1. Phase 1: Use `LWJGL3ify` bridge
    2. Phase 2: Remove bridge and integrate `LWJGL3` directly

### 3.4 Code Style and Collaboration Standards

- **Naming Conventions**:
    - Classes: `PascalCase` (e.g., `JsonConfigManager`)
    - Methods/Variables: `camelCase` (e.g., `loadConfigFromFile`)
    - Mixins: Prefix with `Mixin*` (e.g., `MixinEntityRenderer`)
- **Configuration**:
    - Format: `Gson + JSON`
    - Path: `./starflow/config.json`
    - Supports **hot reload** (no game restart required)
- **Git Collaboration**:
    - Hosted on **GitHub**
    - Pull Requests must include: feature description + security level classification

### 3.5 Community, Open Source, and Compliance

- **License**: `GPL-3.0`
- **Minecraft EULA Compliance**:
  > “The mod is your original work and does not contain any substantial parts of Mojang's code or content... The mod can
  be freely distributed, but distributing a version of Minecraft with the mod is not allowed.”

---

## 4. Open Source Dependencies and Credits

- **[MCP919](https://github.com/Marcelektro/MCP-919)**: Used to obtain Minecraft 1.8.9 decompiled source and resources.
- **[MCP919-LWJGL3-Gradle](https://github.com/Eatgrapes/MCP919-LWJGL3-Gradle)**: Base build environment and integration
  reference.
- **[SpongePowered Mixin](https://github.com/GTNewHorizons/SpongePoweredMixin)**: Core bytecode injection framework.
- **[FPSMaster](https://github.com/WithoutIQ/FPSMaster-v2)**: Reference implementation for rendering and performance
  optimization.
