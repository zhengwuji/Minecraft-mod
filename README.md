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
| **定位物品-怪** | `ItemEntityTracker` | [定位物品-怪](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA) | 默认 `F6` 可视化透视面板，支持搜索怪物、实体、方块、矿石与掉落物，视野中高亮框透视、连线与直线距离显示 |
| **开发者辅助** | `DeveloperHelper` | [开发者辅助](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9) | 全模组自适应可视化 GUI 修改器（默认 `F7` 打开），实时检索与改写生命、护甲、幸运及全 MOD 注册属性数据 |
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

### 0. 🎯 定位物品-怪 (`ItemEntityTracker`)

#### 📌 源码组件说明
- [ItemEntityTracker.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/ItemEntityTracker.java): 模组主入口类，初始化客户端与事件注册。
- [TrackerScreen.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/client/gui/TrackerScreen.java): 可视化配置 GUI，包含【怪物与实体】、【方块与矿石】、【物品与掉落物】、【全局设置】4 大页签，支持实时搜索与 8 种高亮颜色自由切换。
- [WorldRenderHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/client/render/WorldRenderHandler.java): 3D 空间 ESP 渲染引擎，在视距范围内渲染实体/矿石彩框透视、追查连接射线与悬浮距离文本 (`[僵尸] 12.8m`)。
- [TrackerConfig.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/config/TrackerConfig.java): 追踪数据配置映射与 8 种彩框调色盘。

#### 🎮 使用方法
- **快捷键开启**: 游戏内按 **`F6` 键** 唤出 `定位物品-怪` 可视化面板。
- **自定义搜索与切换**: 搜索任意怪物（如 `僵尸`、`骷髅`）、矿石（如 `钻石`、`远古残骸`）或掉落物，一键点击开启追踪并点击切换色彩。
- **3D 视野高亮与距离**: 关闭 GUI 后，视野中将实时显示彩框透视、追踪线与 `XX.Xm` 距离标引！

---

### 1. 🛠️ 开发者辅助 (`DeveloperHelper`)

#### 📌 源码组件说明
- [DevHelper.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/DevHelper.java): 模组主入口，注册网络包与逻辑监听。
- [DevHelperScreen.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/client/gui/DevHelperScreen.java): 可视化全模组自适应面板，包含动态属性搜寻框、可滚动列表、基础值与当前值对照、快捷加点与自定义数值改写提交。
- [DevHelperNetwork.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/network/DevHelperNetwork.java): C2S 网络发包通道，确保客户端与服务端双端 100% 同步属性与生命/饱食度/经验数据。
- [KeyInputHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/client/KeyInputHandler.java): 默认绑定 **`F7` 键** 打开可视化面板。

#### 🎮 使用方法
- **快捷键开启**: 游戏内按 **`F7` 键** 唤出 `开发者辅助` 可视化面板。
- **全模组自适应搜索**: 在搜索框输入任意属性 ID（如 `max_health`, `armor`, `luck`, `attack_damage` 以及任何模组注册属性），实时过滤属性条目。
- **一键快捷修改**: 选中属性后可点击 `+10`、`+100`、`设为 1000` 或手动输入浮点数值提交，服务端与客户端数据瞬间同步生效！

---

### 2. 🛠️ 调试日志 (`DebugLogger`)

#### 📌 源码组件说明
- [DebugLogger.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/DebugLogger.java): 模组入口，注册退出游戏及服务端停止时的自动保存事件。
- [LogCollector.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/util/LogCollector.java): 日志收集与容量管理引擎。支持以 `YYYY年MM月DD日_HH时mm分ss秒.log` 年月日格式保存报告，自动将诊断报告数量限制在 **30 份最新文件** 内。

#### 📂 日志保存路径说明
调试日志模组会自动收集诊断日志并保存至游戏客户端目录下的 **`logs/dev_reports/`** 文件夹中：
- **绝对路径**：`D:\Plain Craft Launcher 2\.minecraft\versions\勇者之章Ⅲ v3.12.15\logs\dev_reports\`
- **运行诊断报告**：`logs/dev_reports/YYYY年MM月DD日_HH时mm分ss秒.log`（包含 JVM 内存、系统硬件、近期 ERROR/Exception 报错摘要，最多保留 30 份）。
- **崩溃拦截明细**：`logs/dev_reports/拦截崩溃断言明细.log`（自动记录被模组安全捕获拦截的堆栈与违规检查，文件满 10MB 自动轮换备份）。

#### 🎮 使用方法
- **快捷键手动导出**: 游戏内按 **`F9` 键** 可随时生成并保存最新的诊断报告至 `logs/dev_reports/` 目录。
- **自动触发备份**: 玩家退出游戏或服务端关闭时，模组会自动导出最新的运行日志报告。

---


## 🔨 编译与构建说明

如需对源码进行修改并重新打包为 Jar 文件：

```bash
# 示例：编译打包 定位物品-怪 MOD
cd "D:\Plain Craft Launcher 2\开发mod源码\定位物品-怪"

# 执行编译打包
./gradlew jar

# 构建产物目录
build/libs/ItemEntityTracker-1.0.0.jar
```

---

## 📜 维护与更新历史

- **2026-07-23**:
  - 新增全新自研模组 **【定位物品-怪 (`ItemEntityTracker`)】**：按 `F6` 键唤出可视化配置面板，支持对全模组怪物、实体、方块与掉落物进行搜索、高亮透视彩框、连接射线与 `XX.Xm` 距离标引。
  - 将编译好的 `[定位物品-怪]ItemEntityTracker-1.0.0.jar` 打包部署至游戏 `mods/` 目录中。
- **2026-07-22**:
  - 升级 **【随身食物BUFF背包 (`FoodBuffBag`)】**：正式突破仅限食物的限制，全面支持将**任意附魔装备/武器/防具/饰品/药水物品**直接放入仓库。放置在仓库内的同类附魔与同类 BUFF 实现了**全量无上限等级累加叠加机制**（例如：放入多件带【锋利 VI】或【力量 I】的装备/食物，对应【力量】BUFF 等级会自动相加升至 13 级以上！），向玩家持续施加强大的无上限永久增益 BUFF！
  - 新增全新自研模组 **【开发者辅助 (`DeveloperHelper`)】**：全模组自适应可视化属性与状态修改 GUI（默认按 `F7` 键唤出），支持生命、护甲、幸运及任意 MOD 注册属性的实时改写。
  - 将编译好的 `[开发者辅助]DeveloperHelper-1.0.0.jar` 与 `[随身食物BUFF背包]FoodBuffBag-1.0.0.jar` 打包部署至游戏 `mods/` 目录中。
