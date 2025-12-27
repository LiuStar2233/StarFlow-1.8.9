# StarFlow 客户端开发记录文档

- **版本**：v2.3.1  
- **最后更新**：2025-12-27  
- **说明**：本文件将作为开发依据。  
- **作者**：LiuStar2233  
- **注**：人人都可以修改本文件以提供开发建议。

> 2025.12.24 平安夜快乐  
> 2025.12.25 圣诞节快乐  

---

## 一、开发记录

### 技术栈选择

| 方案 | 说明 | 备注 |
|------|------|------|
| `MCP919-LWJGL3-Gradle` | 有人维护，支持 `shadowJar`，使用 JDK 21 开发 | mappings 较老（最后更新于 2015–2016 年） |
| `MCP919 + 自行反编译（+LWJGL3）` | — | mappings 较老（同上） |
| `Legacy Fabric Yarn-1.8.9 + LWJGL3` | mappings 较新（最后更新于 2020 年） | **当前选择** |

> **结论**：优先考虑 Yarn，统一使用高版本 JDK 开发，注入框架采用 `SpongePowered Mixin`。

### 功能需求（按重要性与实现难度排序）

- **优化**：参考 **`Sodium(渲染)`**、**`Lithium(逻辑)`**、**`FerrireCore(内存)`**、**`Iris(着色器)`**，这些 `Fabric Mod` 可能对项目的优化有帮助。
- `OverwriteRender`：重写游戏渲染类（最先实现；暂定源码修改，后续迁移到 Mixin `@Overwrite`）。
- `GUIInGame`：重写游戏 GUI 类。
- `BackgroundImage`：主菜单背景自定义（将设计专属 GUI 管理）。
- `Sprint`：在任何地方强制疾跑，不会被打断。
- `KeepDay`：强制客户端显示为白天（仅本地渲染，不改变游戏时间）。
- `Crosshairs`：准星优化 / 自定义。
- `FPS`：帧率显示。
- `CPS`：点击/秒显示。
- `Ping`：延迟显示。
- `Reach`：攻击距离显示。
- `Footprint`：脚印显示。
- `BlockOutline`：方块外边框高亮。
- `KeyDisplay`：按键显示。
- `Logo`：客户端 Logo。
- `AutoLight`：自动亮度调节（游戏内设置滑块）。
- `Skin/Capes`：皮肤 / 披风自定义导入与显示（专属 GUI）。
- `AutoGG`：游戏结束自动输出 “GG”。
- `JsonConfig`：采用 JSON 存储配置。
- `Zoom`：视角缩放。
- `Freelook`：自由视角。
- `NoHurtCamera`：禁用受伤视角抖动。
- `NowTime`：本地时间显示。
- `TNTTime`：TNT 爆炸倒计时。
- `EatTime`：物品食用倒计时（可选）。
- `GameTime`：游戏内时间显示（可选）。
- `PotionEffect`：药水效果持续时间显示。
- `InventorySort`：背包整理（类似 JRE）。
- `EquipmentDisplay`：显示当前穿戴装备。
- `MiniMap`：小地图。
- `EasyInventory`：简易背包操作（如起床战争购方块指定槽位）。
- `Animation`：自定义动画。
- `ChineseInput`：中文输入支持。
- `LockEnglish`：游戏时自动锁定英文输入（输入时除外）。
- `VisualMod`：增强视觉效果。
- `AutoTask`：通过客户端指令实现自动任务（基于 `Command`）。
- `AutoFucking`：自动发送预设骂人消息[doge]（基于 `Command`）。
- `InGameRecord/Replay`：游戏内视频录制。
- `GUI-Adaptive`：为上述 GUI 添加窗口大小自适应。
- `Music`：游戏运行时播放 / 自定义音乐（可导入音频，专属 GUI）。

#### 暂时不考虑

- **`Command`**：客户端指令需前缀（如 `|sfcc`），通过 Mixin 实现本地命令解析（不依赖服务端）。
- **`TabMod`**：仅用于显示本地模组启用状态（如左上角提示），不修改服务端下发数据。
- **`Scoreboard`**：由服务端原生提供，客户端仅可美化显示。
- **`Blur`**：若保留，须用 Shader 实现（可选）。
- **`Physical`**：掉落物物理模拟，考虑移除（复杂且已有成熟方案）。
- **`Multi-Thread`**：多线程运行，实现难度高。
- **`NetworkOptimize`**：网络优化（不改包），但仍有误封风险。
- **`InClash`**：Clash 代理支持，本体已兼容，无需额外处理。

---

## 二、功能需求分类与安全说明

所有功能按 **安全等级** 分为两类：

### 安全类（Safe）

> 仅影响本地渲染、UI、配置或输入，**不发送非常规网络包**，**不模拟玩家行为**，**不会被反作弊识别为异常**。

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
- `MiniMap`（基于本地区块）
- `EasyInventory`（仅 UI 预览，无自动点击）
- `Animation`
- `ChineseInput` / `LockEnglish`
- `VisualMod`
- `Music`
- `GUI-Adaptive`
- `TabMod`（仅本地显示）
- `Blur`（Shader 实现）
- `InClash`

### 高风险类（Risky）

> 可能被反作弊误判、违反服务器规则、或涉及敏感行为逻辑。

- `Sprint`（异常移动）
- `AutoGG`（自动聊天）
- `AutoTask`（自动执行指令）
- `AutoFucking`（自动辱骂）
- `InventorySort`（若实现为自动点击）
- `InGameRecord/Replay`（内存/帧捕获可能触发检测）
- `Physical`（预测类行为）
- `NetworkOptimize`（连接行为异常）
- `Multi-Thread`（破坏反作弊沙箱）

> **策略**：所有高风险功能**默认禁用**，启用时需显式配置，并在 UI 中显示“高风险”警告。

---

## 三、开发与发布规范

### 1. 获取 Yarn 映射与生成命名 JAR

- **首次准备**（仅需一次）：
  ```bash
  git clone https://github.com/Legacy-Fabric/yarn-1.8.9.git
  cd yarn-1.8.9
  ```

- **生成命名 JAR**：
  ```bash
  ./gradlew mapNamedJar
  ```
  输出：`build/libs/1.8.9-named.jar`  
  > 使用 Yarn 清晰命名（如 `net.minecraft.client.Minecraft`）替换混淆名（如 `a.b.c`），未映射部分保留 Intermediary 名。

### 2. 反编译用于参考

- 在 IntelliJ IDEA 中：`File → Open → 选择 1.8.9-named.jar`  
- 使用 **FernFlower**，确保命名一致性。  

### 3. 搭建 Gradle 开发环境

#### 项目结构
```
StarFlow/
├── build.gradle
├── settings.gradle
├── gradle.properties
├── src/main/java/        # Mixin 与功能模块
└── src/main/resources/   # assets、mixin 配置、JSON
```

#### 依赖配置
- **JDK**：Azul Zulu JDK 8（运行时） + JDK 21（开发），**禁用预览特性**
- **Mixin**：GTNH 分支 `SpongePowered Mixin 0.7.11`
- **LWJGL**：初期通过 `LWJGL3ify` 桥接 LWJGL 2 → 3
- **打包**：`ShadowJar` 插件

### 4. 开发与调试流程

- 启动：自定义 `runClient` Gradle 任务
- 原则：
  - 功能模块化（Mixin + JSON 开关 + 配置）
  - 通过安全分类审查
- 支持 **JRebel 热部署**

### 5. 构建与发布要求

#### 构建命令
```bash
./gradlew shadowJar
```

#### 输出命名
```
StarFlow-[VERSION]-[YYYYMMDD].jar
```
示例：`StarFlow-v2.1.0-20251227.jar`

#### 版本语义（`MAJOR.PATCH`）
- **MAJOR**：新增功能（如 `miniMap`）
- **PATCH**：Bug 修复 / 安全优化

### 6. 代码质量与架构原则

- **零“屎山”代码**：
  - 方法 ≤ 3 行
  - 嵌套 ≤ 3 层
  - 类职责单一
- **100% 模块化**
- **注入机制**：全部通过 Mixin 实现  
  > 例外：`overwriteRender` 可暂改源码，但需后续迁移
- **LWJGL 迁移路径**：
  1. 初期：`LWJGL3ify` 桥接
  2. 后期：移除桥接，直连 LWJGL 3

### 7. 代码风格与协作规范

- **命名**：
  - 类：`PascalCase`（如 `JsonConfigManager`）
  - 方法/变量：`camelCase`（如 `loadConfigFromFile`）
  - Mixin：`Mixin*` 前缀（如 `MixinEntityRenderer`）
- **配置**：
  - `Gson + JSON`
  - 路径：`./starflow/config.json`
  - 支持热重载
- **Git 协作**：
  - 使用Github
  - PR 需含：功能说明 + 安全等级

### 8. 社区、开源与合规

- **许可证**：`MIT` 或 `Apache 2.0`
- **Minecraft EULA 合规**：
  > “Mod 是您的原创作品，不包含 Mojang 代码或内容的实质部分……Mod 可自由分发，但不得分发 Minecraft 的 Mod 版本。”  

---

## 四、开源项目使用清单

| 项目 | 用途 | 备注 |
|------|------|------|
| MCP919 | Forge 反编译工具 | 官方已删库，仅支持 1.8.9 |
| Legacy Fabric Yarn | 非官方映射 | 社区维护，已停更 |
| Gradle | 构建工具 | 推荐 8.x |
| LWJGL | 图形/音频/输入 | LWJGL2 → LWJGL3 |
| SpongePowered Mixin (GTNH) | 字节码注入 | 0.7.11，兼容 1.8.9 |
| Gson | JSON 配置 | 轻量 |
| Apache Commons Lang/IO | 工具类 | 原版已含 |
| ShadowJar | 打包 | 生成 fat jar |

---

## 五、附录：参考链接与补充说明

### GitHub 链接
- [GitHub](https://github.com)
- [MCP919-LWJGL3-Gradle](https://github.com/Eatgrapes/MCP919-LWJGL3-Gradle)
- [MCP919（未反编译）](https://github.com/Marcelektro/MCP-919)
- [Legacy Fabric Yarn 1.8.9](https://github.com/Legacy-Fabric/yarn-1.8.9)
- [LWJGL3 升级教程](https://zh.minecraft.wiki/w/Tutorial:%E5%8D%87%E7%BA%A7LWJGL?variant=zh-cn)
- [SpongePowered Mixin (GTNH)](https://github.com/GTNewHorizons/SpongePoweredMixin)
- [ShadowJar](https://github.com/GradleUp/shadow)
- [Gson](https://github.com/google/gson)
- [Apache Commons IO](https://github.com/apache/commons-io)
- [FPSMaster（参考）](https://github.com/WithoutIQ/FPSMaster-v2)
- [Sodium](https://github.com/CaffeineMC/sodium)
- [Lithium](https://github.com/CaffeineMC/lithium)
- [FerriteCore](https://github.com/malte0811/FerriteCore)
- [Iris](https://github.com/IrisShaders/Iris)

### Bilibili 参考
- [如何编写 notification](https://b23.tv/gO5UU2O)
- [Mixin 教程](https://b23.tv/qb4O9fx)、[Mixin 教程 2](https://b23.tv/RODrTQ4)
- [理解 MC 源代码](https://b23.tv/gZI7wgp)
- [StarClient 示例](https://b23.tv/whhOny7)
- [GitHub 访问教程](https://b23.tv/kVCFEPS)
- [FPS Master 设置参考](https://b23.tv/aN9zHAF)
- [LunarClient 参考](https://b23.tv/4T8WFyO)

### 其他资源
- [IntelliJ IDEA](https://www.jetbrains.com/idea/)
- [Azul Zulu JDK](https://www.azul.com/downloads/)
- [Qwen AI](https://chat.qwen.ai/)
- [Minecraft EULA](https://www.minecraft.net/zh-hans/eula)
- [他人写的文档](https://www.328377.xyz/)
- [Minecraft 开发](https://zhuanlan.zhihu.com/p/677090570)

### JRebel 安装与激活（IDEA）

1. **安装插件**  
   `File → Settings → Plugins` → 搜索 `JRebel` → 安装 → 重启。

2. **激活**  
   `Help → JRebel → Activation` → 选择 `Activation Code / Team URL`  
   - 在线生成 GUID：https://www.guidgen.com/  
   - 拼接地址，例如：`http://jrebel.qekang.com/{GUID}`  
   - 填写任意邮箱，确认激活  
   - 勾选 **Work offline**

3. **使用**  
   - 运行配置选择带 JRebel 图标的启动方式  
   - 修改代码后保存即生效（需开启自动编译）

> **我不希望这个项目死在胎中！！！**  
> **拜托活跃一点**

