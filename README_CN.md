# StarFlow 客户端 README

**最后更新时间**：2026/1/1  
**说明**：本文档作为开发指南使用。  
**作者**：LiuStar2233

---

## 1. 开发计划

### 1.1 开发技术栈

我们使用 `MCP919 + LWJGL3 + SpongePowered Mixin + ShadowJar` 以及 `Azul Zulu JDK 8` 开发客户端。

### 1.2 功能列表

**优化参考/目标**：  
- `Sodium`（渲染优化）  
- `Lithium`（逻辑优化）  
- `FerrireCore`（内存优化）  
- `Iris`（着色器优化）

#### 已规划功能（待实现）

- [ ] `OverwriteRender`：重写游戏渲染类（最先实现；初期通过直接修改源码实现，后期添加其他功能时使用 Mixin 的 `@Overwrite`）  
- [ ] `GUIInGame`：重写游戏内 GUI 类（最现实先；初期通过直接修改源码实现，后期添加其他功能时使用 Mixin 的 `@Overwrite`  
- [ ] `BackgroundImage`：自定义主菜单背景（配备专用 GUI 管理器）  
- [ ] `Sprint`：无条件强制疾跑（不受常规限制）  
- [ ] `KeepDay`：强制客户端始终显示白昼（不影响实际游戏时间）  
- [ ] `Crosshairs`：自定义准星样式与增强功能  
- [ ] `FPS`：帧率显示  
- [ ] `CPS`：每秒点击次数显示  
- [ ] `Ping`：网络延迟显示  
- [ ] `Reach`：攻击范围显示  
- [ ] `Footprint`：足迹可视化  
- [ ] `BlockOutline`：高亮方块轮廓  
- [ ] `KeyDisplay`：屏幕键位显示  
- [ ] `Logo`：客户端 Logo 渲染  
- [ ] `AutoLight`：自动亮度调节（可选）  
- [ ] `Skin/Capes`：自定义皮肤与披风导入/显示（配备专用 GUI）  
- [ ] `AutoGG`：游戏结束时自动发送“GG”  
- [ ] `JsonConfig`：使用 JSON 存储配置  
- [ ] `Zoom`：镜头缩放功能  
- [ ] `Freelook`：自由视角模式  
- [ ] `NoHurtCamera`：受伤时不抖动镜头  
- [ ] `NowTime`：显示本地系统时间  
- [ ] `TNTTime`：TNT 爆炸倒计时  
- [ ] `EatTime`：物品食用倒计时（可选）  
- [ ] `GameTime`：游戏内时间显示（可选）  
- [ ] `PotionEffect`：药水效果剩余时间显示  
- [ ] `InventorySort`：物品栏自动整理（类似 JEI）  
- [ ] `EquipmentDisplay`：显示当前穿戴的盔甲  
- [ ] `MiniMap`：小地图实现  
- [ ] `EasyInventory`：简化物品栏交互（例如在 BedWars 中快速购买方块到指定槽位）  
- [ ] `Animation`：支持自定义动画  
- [ ] `UTF-8Input`：支持输入UTF-8字符  
- [ ] `LockEnglish`：游戏过程中自动锁定为英文输入（打字输入时除外）  
- [ ] `VisualMod`：增强视觉效果（如粒子、阴影、环境光）  
- [ ] `AutoTask`：通过客户端命令自动执行任务（基于 `ClientCommand` ）  
- [ ] `AutoTalk`：自动发送预设的消息 [doge]（基于 `ClientCommand` ，可归类到 `AutoTask`）  
- [ ] `InGameRecord/Replay`：游戏内录像与回放  
- [ ] `GUI-Adaptive`：所有 GUI 根据窗口尺寸自适应布局  
- [ ] `Music`：游戏内背景音乐播放，支持自定义曲目（配备专用 GUI）

#### 暂不计划实现的功能

- `ClientCommand`：客户端命令需带前缀（例如 `|sfcc`）。本地通过 Mixin 实现，无需服务器支持。  
- `TabMod`：仅显示本地模组状态（如左上角指示灯），不修改服务器下发的 Tab 列表。  
- `Scoreboard`：由服务器原生提供，客户端仅增强视觉表现。  
- `Blur`：若保留，须通过着色器实现（可选）。  
- `Physical`：物品掉落物理模拟；因复杂度高且已有解决方案，大概率弃用。  
- `Multi-Thread`：多线程执行；实现难度高。  
- `NetworkOptimize`：网络优化（不修改数据包），但仍存在误封风险。  
- `InClash`：Clash 代理支持；已开箱即用，无需额外处理。

### 1.3目前开发流程/阶段
将 `gradle.build` 作为 `build` 文件基础（这是原版所需依赖）尝试启动客户端，即 **`gradle task`：`runClient`**。  
若成功：替换为 `gradle.build.1` 作为 `build` 文件进行开发（这是我们更新优化后的依赖，想要使用必须重写源代码），即 `OverwriteRender、GUIInGame、上述优化` 等功能。  
若失败：修改 `gradle.build` 保证 **`gradle task`：`runClient`** 能正常使用。

---

## 2. 功能分类与安全指南

所有功能按安全性分为两类：

### ✅ 安全功能

这些功能**仅影响本地客户端**，不修改网络通信、不模拟玩家行为，不会触发反作弊检测：

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

### ⚠️ 高风险功能

这些功能**可能被反作弊系统判定为违规**，默认关闭：

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

### 3. 开发与发布规范

#### 3.1 开发环境配置

- **JDK**：Azul Zulu JDK 8（禁止使用预览版或高版本）
- **Mixin 框架**：SpongePowered Mixin 0.7.11
- **图形库**：
  - 阶段 1：通过 `LWJGL3ify` 桥接兼容 LWJGL3
  - 阶段 2：移除桥接，直接集成原生 LWJGL3
- **打包工具**：ShadowJar

> 说明：所有开发应在此环境组合下进行，以确保兼容性与构建一致性。

---

#### 3.2 代码质量与架构原则

- **模块化**：实现 **100% 模块化设计**，每个功能应为独立、可插拔的模块。
- **实现方式**：
  - 所有功能 **必须基于 Mixin 实现**
  - **唯一例外**：`OverwriteRender` 与 `GUIInGame` 初期允许直接修改源码，后续需迁移至 Mixin 的 `@Overwrite`
- **代码风格**：
  - 类名：`PascalCase`（例：`JsonConfigManager`）
  - 方法/变量名：`camelCase`（例：`loadConfigFromFile`）
  - Mixin 类：前缀为 `Mixin*`（例：`MixinEntityRenderer`）
- **拒绝垃圾代码**：保持高可读性、高可维护性，禁止冗余、硬编码或魔法值。

---

#### 3.3 配置系统规范

- **格式**：使用 `Gson + JSON`
- **路径**：`./starflow/config.json`
- **功能要求**：
  - 支持 **热重载**（无需重启游戏即可生效）
  - 所有可配置项必须提供默认值，并在首次启动时自动生成配置文件

---

#### 3.4 构建与发布流程

- **构建命令**：
  ```bash
  ./gradlew shadowJar
  ```
- **输出文件命名格式**：
  ```
  StarFlow-[VERSION]-[YYYYMMDD].jar
  ```
  示例：`StarFlow-v2.1.0-20251227.jar`
- **版本语义（MAJOR.PATCH）**：
  - `MAJOR`：新增功能（如 `MiniMap`）
  - `PATCH`：Bug 修复或安全性优化

> 注意：版本号更新应与功能变更严格对应，禁止跳号或滥用语义。

---

#### 3.5 项目协作规范

- **代码托管**：GitHub [StarFlow-1.8.9](https://github.com/LiuStar2233/StarFlow-1.8.9)
- **Pull Request 要求**：
  - 应包含 **功能描述**
  - 应标注 **安全等级分类**
- **开发者沟通**：开发者群暂未设立，以 GitHub Issues / PR 为主沟通渠道

---

#### 3.6 开源合规与许可证

- **许可证**：GPL-3.0
- **遵守 Minecraft EULA**：
  > “此模组为你原创作品，不包含 Mojang 代码或内容的实质性部分…… 模组可自由分发，但不允许分发附带此模组的 Minecraft 完整版本。”

---

## 4. 开源依赖与致谢

- [MCP919](https://github.com/Marcelektro/MCP-919)：用于获取 Minecraft 1.8.9 的反编译源码与资源  
- [MCP919-LWJGL3-Gradle](https://github.com/Eatgrapes/MCP919-LWJGL3-Gradle)：基础构建环境与集成参考  
- [SpongePowered Mixin](https://github.com/GTNewHorizons/SpongePoweredMixin)：核心字节码注入框架  
- [FPSMaster](https://github.com/SuperSkidder/FPSMaster)：渲染与性能优化的参考实现
- [ULAchelous](https://github.com/ULAchelous)：团队技术骨干
- [LiuStar2233](https://github.com/LiuStar2233)：团队技术骨干
- 以及我们的测试人员

--- 