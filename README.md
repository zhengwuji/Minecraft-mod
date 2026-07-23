# 🛠️ 整合包 MOD 开发源码与使用维护指南 (Minecraft 1.20.1 Forge)

欢迎使用 **勇者之章Ⅲ 整合包自研 MOD 源码仓库**！本仓库包含了为 《勇者之章Ⅲ v3.12.15》 整合包专属定制开发、优化与崩溃防护的全部 MOD 项目源码、功能架构解析与详细使用说明。

> [!IMPORTANT]
> **核心调试与诊断规范 (DebugLogger Diagnostic & Logging Rule)**
> 1. **调试日志优先**: 遇到任何游戏问题或崩溃时，必须**优先查看【调试日志 MOD】（`DebugLogger`）输出的完整调试日志/诊断报告（`logs/dev_reports/` 与 `logs/latest.log`）**，结合真实日志证据精准定位根源并针对性修复。
> 2. **日志动态补充**: 若当前日志中未包含问题的详细上下文或对应日志，**必须第一时间向【调试日志 MOD】（`d:\Plain Craft Launcher 2\开发mod源码\调试日志`）添加更多相关的调试日志打印与异常捕获逻辑**，重新编译部署后获取完整的运行轨迹，绝不凭空推测！

> [!NOTE]
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
| **调试日志** | `DebugLogger` | [调试日志](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97) | 纯后台全自动运行引擎（已移除前台按键），全量日志捕获、自动化诊断报告、全模组底层崩溃拦截（Malum 渲染空指针防护 & JEI 物品堆叠修正） |
| **随身食物BUFF背包** | `FoodBuffBag` | [随身食物BUFF背包](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%9A%8F%E8%BA%AB%E9%A3%9F%E7%89%A9BUFF%E8%83%8C%E5%8C%85) | 随身食物自动消耗、BUFF 维持与专属食物存储背包 |
| **双击W自动奔跑** | `AutoRun` | [双击W自动奔跑](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%8F%8C%E5%87%BBW%E8%87%AA%E5%8A%A8%E5%A5%9D%E8%B7%91) | 双击 W 触发长途自动疾跑/自动前进行走优化 |
| **附魔等级上限突破** | `ELB` | [附魔等级上限突破](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%99%84%E9%AD%94%E7%AD%89%E7%BA%A7%E4%B8%8A%E9%99%90%E7%AA%81%E7%A0%B4) | 突破原版附魔等级限制，支持超高等级附魔合成与显示 |
| **超级矿石** | `SuperOres` | [超级矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B6%85%E7%BA%A7%E7%AF%BF%E7%9F%B3) | 超级倍率资源矿石块及其熔炼/采集生成控制 |
| **经验矿石** | `ExpOre` | [经验矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E7%BB%8F%E9%AA%8C%E7%AF%BF%E7%9F%B3) | 专有经验矿石生成与高额经验球掉落机制 |
| **强化工具** | `ReinforcedTools` | [强化工具](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%8A%E5%8C%96%E5%B7%A5%E5%85%B7) | 多阶强力强化装备与特殊挖掘工具支持 |
| **敌对神经网络** | `HostileNetworks` | [敌对神经网络自动获取生物掉落物](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E6%95%8C%E5%AF%B9%E7%A5%9E%E7%BB%8F%E7%BD%91%E7%BB%9C%E8%87%AA%E5%8A%A8%E8%8E%B7%E5%8F%96%E7%94%9F%E7%89%A9%E6%8E%89%E8%90%BD%E7%89%A9) | 自动化战利品预测模型与无人化掉落物产出适配 |
| **钻石工作台** | `DiamondTable` | [钻石工作台](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%92%BB%E7%9F%B3%E5%B7%A5%E4%BD%9C%E5%B7%A5%E5%8F%B0) | 高级配方合成台与多目标批量合成 |

---

## 📜 维护与更新历史

- **2026-07-24**:
  - 全面移除 **【调试日志 (`DebugLogger`)】** 前台按键绑定与浮窗（完全纯后台全自动化运行，自动侦测并记录诊断报告）。
  - 建立 **【调试日志优先诊断与日志动态扩充】** 全局开发规范。
  - 重构 **【调试日志 (`DebugLogger`)】** 崩溃防护系统：
    - 集成 Lodestone 渲染层与 Malum 模组的 `LodestoneRenderLayer` 手动实例化死锁兜底防护。
    - 结合 `Throwable` 级防崩拦截与 `MixinCreativeModeTab` 物品堆叠修正，彻底消除创造页签与 JEI 构建过程中的崩溃泄露，保障 TACZ 枪械与全模组物品列表平稳加载。
    - 重新编译打包 `[调试日志]DebugLogger-1.0.0.jar` 并覆盖更新至 `mods/` 文件夹。
- **2026-07-23**:
  - 新增全新自研模组 **【定位物品-怪 (`ItemEntityTracker`)】**。
