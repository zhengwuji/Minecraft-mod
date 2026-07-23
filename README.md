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
| **自定义等价交换EMC** | `CustomEMC` | [自定义等价交换EMC](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC) | 默认 `F8` 呼出现代化 GUI 面板（按键设置汉化分类为`自定义等价交换EMC`，支持自定义按键与组合键）；自适应全 MOD 物品兜底价格（默认 5555），支持抓取主手物品 ID 与一键重载 ProjectE EMC |
| **定位物品-怪** | `ItemEntityTracker` | [定位物品-怪](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA) | 默认 `F6` 可视化透视面板，支持搜索怪物、实体、方块、矿石与掉落物，视野中高亮框透视、连线与直线距离显示 |
| **开发者辅助** | `DeveloperHelper` | [开发者辅助](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9) | 全模组自适应可视化 GUI 修改器（默认 `F7` 打开），实时检索与改写生命、护甲、幸运及全 MOD 注册属性数据 |
| **调试日志** | `DebugLogger` | [调试日志](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97) | 全量日志捕获、F9 诊断快照、全模组底层崩溃拦截（整合 Malum 渲染空指针防护 & JEI/创造页签物品构件拦截） |
| **随身食物BUFF背包** | `FoodBuffBag` | [随身食物BUFF背包](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%9A%8F%E8%BA%AB%E9%A3%9F%E7%89%A9BUFF%E8%83%8C%E5%8C%85) | 随身食物自动消耗、BUFF 维持与专属食物存储背包 |
| **双击W自动奔跑** | `AutoRun` | [双击W自动奔跑](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%8F%8C%E5%87%BBW%E8%87%AA%E5%8A%A8%E5%A5%9D%E8%B7%91) | 双击 W 触发长途自动疾跑/自动前进行走优化 |
| **附魔等级上限突破** | `ELB` | [附魔等级上限突破](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%99%84%E9%AD%94%E7%AD%89%E7%BA%A7%E4%B8%8A%E9%99%90%E7%AA%81%E7%A0%B4) | 突破原版附魔等级限制，支持超高等级附魔合成与显示 |
| **超级矿石** | `SuperOres` | [超级矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B6%85%E7%BA%A7%E7%AF%BF%E7%9F%B3) | 超级倍率资源矿石块及其熔炼/采集生成控制 |
| **经验矿石** | `ExpOre` | [经验矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E7%BB%8F%E9%AA%8C%E7%AF%BF%E7%9F%B3) | 专有经验矿石生成与高额经验球掉落机制 |
| **强化工具** | `ReinforcedTools` | [强化工具](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%8A%E5%8C%96%E5%B7%A5%E5%85%B7) | 多阶强力强化装备与特殊挖掘工具支持 |
| **敌对神经网络** | `HostileNetworks` | [敌对神经网络自动获取生物掉落物](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E6%95%8C%E5%AF%B9%E7%A5%9E%E7%BB%8F%E7%BD%91%E7%BB%9C%E8%87%AA%E5%8A%A8%E8%8E%B7%E5%8F%96%E7%94%9F%E7%89%A9%E6%8E%89%E8%90%BD%E7%89%A9) | 自动化战利品预测模型与无人化掉落物产出适配 |
| **钻石工作台** | `DiamondTable` | [钻石工作台](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%92%BB%E7%9F%B3%E5%B7%A5%E4%BD%9C%E5%B7%A5%E5%8F%B0) | 高级配方合成台与多目标批量合成 |

---

## 🔍 各 MOD 源码功能与详情使用手册

### 0. ⚡ 自定义等价交换EMC (`CustomEMC`)

#### 📌 源码组件说明
- [CustomEMCMod.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC/src/main/java/com/customemc/CustomEMCMod.java): 模组主入口，负责初始化与事件总线监听。
- [CustomEMCMapper.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC/src/main/java/com/customemc/CustomEMCMapper.java): 继承 `IEMCMapper`，实现对 ProjectE EMC 拓扑树的动态注入。支持全局无价格物品的默认 5555 EMC 自动补充（`setValueAfter`）以及自定义指定物品 EMC 的强行覆写（`setValueBefore`）。
- [ConfigManager.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC/src/main/java/com/customemc/ConfigManager.java): 配置文件数据持久化（读写 `.minecraft/config/custom_emc.json`），自动格式化生成默认规则。
- [CustomEMCScreen.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC/src/main/java/com/customemc/client/gui/CustomEMCScreen.java): 现代化深色玻璃质感控制面板 GUI。支持实时修改默认 EMC、一键抓取主手物品 ID、指定物品价格修改与自动发送指令重载 ProjectE EMC。
- [KeyInit.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC/src/main/java/com/customemc/client/KeyInit.java): 注册 `F8` 打开 GUI 的按键绑定，分类汉化为 **`自定义等价交换EMC`**，原生支持在原版控制菜单中重绑定单键或组合键。

---

### 1. 🎯 定位物品-怪 (`ItemEntityTracker`)

#### 📌 源码组件说明
- [ItemEntityTracker.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/ItemEntityTracker.java): 模组主入口类，初始化客户端与事件注册。
- [TrackerScreen.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/client/gui/TrackerScreen.java): 可视化配置 GUI，包含【怪物与实体】、【方块与矿石】、【物品与掉落物】、【全局设置】4 大页签，支持实时搜索与 8 种高亮颜色自由切换。
- [WorldRenderHandler.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA/src/main/java/com/antigravity/tracker/client/render/WorldRenderHandler.java): 3D 空间 ESP 渲染引擎，在视距范围内渲染实体/矿石彩框透视、追查连接射线与悬浮距离文本 (`[僵尸] 12.8m`)。

---

### 2. 🛠️ 开发者辅助 (`DeveloperHelper`)

#### 📌 源码组件说明
- [DevHelper.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/DevHelper.java): 模组主入口，注册网络包与逻辑监听。
- [DevHelperScreen.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9/src/main/java/com/antigravity/devhelper/client/gui/DevHelperScreen.java): 可视化全模组自适应面板，包含动态属性搜寻框、可滚动列表、基础值与当前值对照、快捷加点与自定义数值改写提交。

---

### 3. 🛠️ 调试日志 (`DebugLogger`)

#### 📌 源码组件说明
- [DebugLogger.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/DebugLogger.java): 模组入口，注册退出游戏及服务端停止时的自动保存事件。
- [MixinSpiritCrucibleRenderer.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/mixin/MixinSpiritCrucibleRenderer.java) / [MixinVoidDepotRenderer.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/mixin/MixinVoidDepotRenderer.java): 自动在客户端类加载阶段对 Lodestone 的 `RenderHandler.DELAYED_RENDER` 进行提前初始化防护，彻底防护掉 Malum 邪恶魔法邪物在静态初始化时的空指针崩溃（`NullPointerException`）。
- [MixinCreativeModeTab.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/mixin/MixinCreativeModeTab.java): 在后台自动拦截并修正所有 count 异常的违规堆叠物品，防止 JEI 扫描创造页签时崩溃卡死导致全枪械与物品索引丢失。
- [LogCollector.java](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97/src/main/java/com/antigravity/debuglogger/util/LogCollector.java): 日志收集与容量管理引擎。自动将诊断报告限制在 30 份最新文件内。

---

## 📜 维护与更新历史

- **2026-07-24**:
  - 全面重构 **【调试日志 (`DebugLogger`)】** 崩溃防护系统：
    - 全量迁移并集成了 Lodestone 渲染层的 `MixinRenderHandler`、Malum 邪物模组的 `MixinSpiritCrucibleRenderer` / `MixinVoidDepotRenderer`。
    - 结合 `Throwable` 级防崩拦截与 `MixinCreativeModeTab` 物品堆叠修正，彻底消除创造页签与 JEI 构建过程中的崩溃泄露，保障 TACZ 枪械与全模组物品列表平稳加载。
    - 重新编译打包 `[调试日志]DebugLogger-1.0.0.jar` 并覆盖更新至 `mods/` 文件夹。
  - 新增全新自研模组 **【自定义等价交换EMC (`CustomEMC`)】**，支持按 `F8` 打开现代风 GUI 面板（按键分类汉化为 `自定义等价交换EMC`），并可一键重载 ProjectE EMC。
  - 安全归档并清理移除了多余的 `QuadHotbar` 源码与模组。
- **2026-07-23**:
  - 新增全新自研模组 **【定位物品-怪 (`ItemEntityTracker`)】**。
- **2026-07-22**:
  - 升级 **【随身食物BUFF背包 (`FoodBuffBag`)】** 与 **【开发者辅助 (`DeveloperHelper`)】**。
