# 🛠️ 整合包 MOD 开发源码与使用维护指南 (Minecraft 1.20.1 Forge)

欢迎使用 **勇者之章Ⅲ 整合包自研 MOD 源码仓库**！本仓库包含了为 《勇者之章Ⅲ v3.12.15》 整合包专属定制开发、优化与崩溃防护的全部 MOD 项目源码、功能架构解析与详细使用说明。

> [!IMPORTANT]
> **开发环境说明**
> - **Minecraft 版本**: `1.20.1`
> - **Forge API 版本**: `47.4.13`
> - **Java SDK**: `JDK 17` (Eclipse Adoptium)
> - **Build 工具**: Gradle 8.1.1 + ForgeGradle 6.0+

---

## 📚 项目目录与自研 MOD 清单

| 模组名称 | 中文标识 | 源码路径 | 核心功能概述 |
| :--- | :--- | :--- | :--- |
| **调试日志** | `DebugLogger` | [调试日志](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97) | 开发者专属全量日志捕获、F9 诊断快照、断言崩盘自动拦截与容量管理 |
| **多重快捷栏** | `QuadHotbar` | [多重快捷栏](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F) | 4 层 36 槽位原生单行快捷栏快捷轮换，内置 Lodestone / ModCompat 崩溃补丁 |
| **随身食物BUFF背包** | `FoodBuffBag` | [随身食物BUFF背包](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%9A%8F%E8%BA%AB%E9%A3%9F%E7%89%A9BUFF%E8%83%8C%E5%8C%85) | 随身食物自动消耗、BUFF 维持与专属食物存储背包 |
| **双击W自动奔跑** | `AutoRun` | [双击W自动奔跑](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%8F%8C%E5%87%BBW%E8%87%AA%E5%8A%A8%E5%A5%9D%E8%B7%91) | 双击 W 触发长途自动疾跑/自动前进行走优化 |
| **附魔等级上限突破** | `ELB` | [附魔等级上限突破](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%99%84%E9%AD%94%E7%AD%89%E7%BA%A7%E4%B8%8A%E9%99%90%E7%AA%81%E7%A0%B4) | 突破原版附魔等级限制，支持超高等级附魔合成与显示 |
| **超级矿石** | `SuperOres` | [超级矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B6%85%E7%BA%A7%E7%AF%BF%E7%9F%B3) | 超级倍率资源矿石块及其熔炼/采集生成控制 |
| **经验矿石** | `ExpOre` | [经验矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E7%BB%8F%E9%AA%8C%E7%AF%BF%E7%9F%B3) | 专有经验矿石生成与高额经验球掉落机制 |
| **强化工具** | `ReinforcedTools` | [强化工具](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%8A%E5%8C%96%E5%B7%A5%E5%85%B7) | 多阶强力强化装备与特殊挖掘工具支持 |
| **敌对神经网络** | `HostileNetworks` | [敌对神经网络自动获取生物掉落物](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E6%95%8C%E5%AF%B9%E7%A5%9E%E7%BB%8F%E7%BD%91%E7%BB%9C%E8%87%AA%E5%8A%A8%E8%8E%B7%E5%8F%96%E7%94%9F%E7%89%A9%E6%8E%89%E8%90%BD%E7%89%A9) | 自动化战利品预测模型与无人化掉落物产出适配 |
| **钻石工作台** | `DiamondTable` | [钻石工作台](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%92%BB%E7%9F%B3%E5%B7%A5%E4%BD%9C%E5%8F%B0) | 高级配方合成台与多目标批量合成 |

---

## 🔍 各 MOD 源码功能与详情使用手册

### 1. 🛠️ 调试日志 (`DebugLogger`)

#### 📌 源码组件说明
- [DebugLogger.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/DebugLogger.java): 模组入口，注册退出游戏 (`ClientPlayerNetworkEvent.LoggingOut`) 及服务端停止 (`ServerStoppingEvent`) 时的自动保存事件。
- [LogCollector.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/util/LogCollector.java): 日志收集与容量管理引擎。支持以 `YYYY年MM月DD日_HH时mm分ss秒.log` 年月日格式保存报告，自动将诊断报告数量限制在 **30 份最新文件** 内（超出自动按时间排序删除老文件）。
- [MixinForgeHooksDebugHunter.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/mixin/MixinForgeHooksDebugHunter.java): 捕获并拦截【混沌降生 born_in_chaos】等模组在创造模式按 `E` 触发的硬性断言崩溃，并静默写入 `logs/dev_reports/拦截崩溃断言明细.log`。
- [KeyInputHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/client/KeyInputHandler.java): 绑定 `F9` 快捷键，主动导出诊断报告。
- [DebugOverlay.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/client/DebugOverlay.java): 屏幕左上角轻量级实时开发者 HUD 调试信息浮窗，全反射安全适配。

#### 📂 日志自动保存路径详情
| 日志类型 | 相对路径 | 整合包版本绝对物理路径 | 文件名格式 / 说明 |
| :--- | :--- | :--- | :--- |
| **开发者诊断报告** | `logs/dev_reports/` | `D:\Plain Craft Launcher 2\.minecraft\versions\勇者之章Ⅲ v3.12.15\logs\dev_reports\` | `YYYY年MM月DD日_HH时mm分ss秒.log` (如 `2026年07月22日_12时03分24秒.log`) |
| **拦截崩溃明细** | `logs/dev_reports/` | `D:\Plain Craft Launcher 2\.minecraft\versions\勇者之章Ⅲ v3.12.15\logs\dev_reports\` | `拦截崩溃断言明细.log` (超过 10MB 自动轮换备份) |
| **原厂运行全量日志**| `logs/` | `D:\Plain Craft Launcher 2\.minecraft\versions\勇者之章Ⅲ v3.12.15\logs\` | `latest.log` 和 `debug.log` |
| **崩溃原生报告** | `crash-reports/` | `D:\Plain Craft Launcher 2\.minecraft\versions\勇者之章Ⅲ v3.12.15\crash-reports\` | `crash-YYYY-MM-DD_HH.mm.ss-client.txt` |

#### 🎮 使用方法
- **快捷按键**: 游戏内按 `F9` 键可立即在上述 `logs/dev_reports/` 目录中生成精炼快照。
- **全自动模式**: 无需按任何按键，每次退出世界或关闭客户端，系统会自动生成以当前 `年月日_时分秒` 命名的日志文件。
- **自动化容量保护**: `dev_reports` 目录超过 30 份报告时，旧文件会自动清理，零空间占用负担。


---

### 2. 🎒 多重快捷栏 (`QuadHotbar`)

#### 📌 源码组件说明
- [QuadHotbar.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/QuadHotbar.java): 主模组入口与网络通道注册。
- [HotbarInventoryLogic.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/logic/HotbarInventoryLogic.java): 快捷栏槽位算法逻辑，实现原版玩家背包第一排（槽 0-8）与后续 3 排背包槽位（9-35）之间的双向平滑轮换。
- [KeyInputHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/client/KeyInputHandler.java): 快捷键监听（`V` 键向下翻页轮换，`B` 键向上翻页轮换），注入全环境 Searge 混淆安全反射防御。
- [HotbarHudOverlay.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/client/HotbarHudOverlay.java): 按照用户最新需求，禁用额外的第二行快捷栏渲染，维持原版单行快捷栏画面风格。
- [MixinRenderHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/mixin/MixinRenderHandler.java) & [MixinVoidDepotRenderer.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/mixin/MixinVoidDepotRenderer.java): 解决渲染引擎 `Lodestone` 中 `RenderHandler` 的 NullPointerException 崩溃补丁。
- [MixinModCompat.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%A4%9A%E9%87%8D%E5%BF%AB%E6%8D%B7%E6%A0%8F/src/main/java/com/antigravity/quadhotbar/mixin/MixinModCompat.java): 捕获并防御 `SophisticatedCore` 在缺失 `reliquary` 类时的 `NoClassDefFoundError`。

#### 🎮 使用方法
- **下翻页轮换**: 默认按 `V` 键，将当前快捷栏物品与背包下一行平滑切换。
- **上翻页轮换**: 默认按 `B` 键，向上轮换快捷栏物品。
- **外观呈现**: 原生单行 GUI，画面不产生错位与杂乱遮挡。

---

### 3. 🍱 随身食物BUFF背包 (`FoodBuffBag`)

#### 📌 源码组件说明
- [FoodBuffBag.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%9A%8F%E8%BA%AB%E9%A3%9F%E7%89%A9BUFF%E8%83%8C%E5%8C%85/src/main/java/com/antigravity/foodbuffbag/FoodBuffBag.java): 主初始化类。
- `capability/`: 储存玩家随身食物背包槽位数据与自动食用逻辑。
- `event/`: 玩家饥饿度降低时自动检测背包食物并施加药水 BUFF 的 Tick 监听器。

#### 🎮 使用方法
- 将食物放入专用的随身食物背包中，当玩家进入战斗、消耗饥饿度或掉血时，食物背包会自动按最优策略消耗食物并提供持续的增益 BUFF。

---

### 4. 🏃 双击W自动奔跑 (`AutoRun`)

#### 📌 源码组件说明
- 监听玩家键盘 `W` 键的两次按下时间间隔（默认低于 250ms），触发后开启连续自动前进/长途疾跑状态，按 `S` 键或再按一次 `W` 即可取消。

#### 🎮 使用方法
- 快速双击 `W` 键，即可松开双手实现自动前进行走与奔跑。

---

### 5. 🔮 附魔等级上限突破 (`ELB`)

#### 📌 源码组件说明
- 解除原版 `Enchantment.getMaxLevel()` 硬编码限制，支持通过铁砧与指令将锋利、保护等附魔提升至 XV（15级）甚至更高级别，并处理文本颜色的渲染。

---

## 🔨 编译与构建说明

如需对源码进行修改并重新打包为 Jar 文件：

```bash
# 1. 切换至对应 MOD 源码目录（示例为 调试日志）
cd "D:\Plain Craft Launcher 2\开发mod源码\调试日志"

# 2. 执行编译打包
./gradlew jar

# 3. 构建产物目录
build/libs/DebugLogger-1.0.0.jar
```

> [!NOTE]
> 编译成功后，将生成的 Jar 文件复制部署至 `.minecraft/versions/勇者之章Ⅲ v3.12.15/mods/` 即可生效。

---

## 📜 维护与更新历史

- **2026-07-22**:
  - 创建独立 **【调试日志 (`DebugLogger`)】** MOD，剥离 QuadHotbar 中的调试开销。
  - 实现调试报告的**全自动保存**、**年月日时间命名**与 **30 份文件上限智能清理**。
  - 修复 `GuiGraphics.pose()` 与 `ItemStack.getCount()` 在 Forge 混淆环境下的 `NoSuchMethodError` 报错。
  - 修复 `Lodestone` NPE 与 `SophisticatedCore` 缺失类崩溃。
