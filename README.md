# 🛠️ 整合包 MOD 开发源码与使用维护指南 (Minecraft 1.20.1 Forge)

欢迎使用 **勇者之章Ⅲ 整合包自研 MOD 源码仓库**！本仓库包含了为 《勇者之章Ⅲ v3.12.15》 整合包专属定制开发、优化与崩溃防护的全部 MOD 项目源码、功能架构解析与详细使用说明。

> [!IMPORTANT]
> **核心调试与诊断规范 (DebugLogger Diagnostic & Logging Rule)**
> 1. **单一定位**: 【调试日志 MOD】（`DebugLogger`）只能用于查看/收集运行日志与运行诊断，**严禁向该 MOD 中写入任何玩法、机制或逻辑拦截注入/修复功能**。所有功能性修改必须在各自专门的 MOD（如 CustomEMC、elb 等）中实现！
> 2. **日志优先诊断原则（最高优先级）**: 遇到任何游戏问题、卡顿或崩溃时，**第一动作必须先查看【调试日志 MOD】（`DebugLogger`）输出的最新日志与诊断报告（`logs/dev_reports/` 与 `logs/latest.log`）**，从中获取确凿的错误堆栈信息后，再在专门 MOD 中进行针对性修复！

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
| **自定义等价交换EMC** | `CustomEMC` | [自定义等价交换EMC](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%87%AA%E5%AE%9A%E4%B9%89%E7%AD%89%E4%BB%B7%E4%BA%A4%E6%8D%A2EMC) | 默认 `F8` 呼出现代化 GUI 面板；全自动为玩家解锁 ProjectE 全知识库 (`AutoKnowledgeHandler`)；自适应全 MOD 物品缺省 5555 EMC；集成 `MixinSlotTypeMessageBuilder` 字节码拦截，硬核解封【货币战争饰品 (`currency_wars_curios` / `wallet`)】槽位上限至 15 个 |
| **定位物品-怪** | `ItemEntityTracker` | [定位物品-怪](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%AE%9A%E4%BD%8D%E7%89%A9%E5%93%81-%E6%80%AA) | 默认 `F6` 可视化透视面板，支持搜索怪物、实体、方块、矿石与掉落物，视野中高亮框透视、连线与直线距离显示 |
| **开发者辅助** | `DeveloperHelper` | [开发者辅助](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%80%E5%8F%91%E8%80%85%E8%BE%85%E5%8A%A9) | 全模组自适应可视化 GUI 修改器（默认 `F7` 打开），实时检索与改写生命、护甲、幸运及全 MOD 注册属性数据 |
| **调试日志** | `DebugLogger` | [调试日志](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B0%83%E8%AF%95%E6%97%A5%E5%BF%97) | 纯后台全自动运行诊断引擎，全量日志捕获、自动化诊断报告、内置 `CuriosDebugTracker` 槽位与命令诊断监听器、创造模式物品栏空数据防爆防护 |
| **随身食物BUFF背包** | `FoodBuffBag` | [随身食物BUFF背包](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%9A%8F%E8%BA%AB%E9%A3%9F%E7%89%A9BUFF%E8%83%8C%E5%8C%85) | 随身食物自动消耗、BUFF 维持与专属食物存储背包 |
| **双击W自动奔跑** | `AutoRun` | [双击W自动奔跑](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%8F%8C%E5%87%BBW%E8%87%AA%E5%8A%A8%E5%A5%9D%E8%B7%91) | 双击 W 触发长途自动疾跑/自动前进行走优化 |
| **附魔等级上限突破** | `ELB` | [附魔等级上限突破](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%99%84%E9%AD%94%E7%AD%89%E7%BA%A7%E4%B8%8A%E9%99%90%E7%AA%81%E7%A0%B4) | 突破原版附魔等级限制，优雅 Mixin 移植高级附魔台（EnchantingPlus）0 经验消耗与全附魔解除互斥机制 |
| **超级矿石** | `SuperOres` | [超级矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E8%B6%85%E7%BA%A7%E7%AF%BF%E7%9F%B3) | 超级倍率资源矿石块及其熔炼/采集生成控制 |
| **经验矿石** | `ExpOre` | [经验矿石](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E7%BB%8F%E9%AA%8C%E7%AF%BF%E7%9F%B3) | 专有经验矿石生成与高额经验球掉落机制 |
| **强化工具** | `ReinforcedTools` | [强化工具](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E5%BC%8A%E5%8C%96%E5%B7%A5%E5%85%B7) | 多阶强力强化装备与特殊挖掘工具支持 |
| **敌对神经网络** | `HostileNetworks` | [敌对神经网络自动获取生物掉落物](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E6%95%8C%E5%AF%B9%E7%A5%9E%E7%BB%8F%E7%BD%91%E7%BB%9C%E8%87%AA%E5%8A%A8%E8%8E%B7%E5%8F%96%E7%94%9F%E7%89%A9%E6%8E%89%E8%90%BD%E7%89%A9) | 自动化战利品预测模型与无人化掉落物产出适配 |
| **钻石工作台** | `DiamondTable` | [钻石工作台](file:///D:/Plain%20Craft%20Launcher%202/%E5%BC%80%E5%8F%91mod%E6%BA%90%E7%A0%81/%E9%92%BB%E7%9F%B3%E5%B7%A5%E4%BD%9C%E5%B7%A5%E5%8F%B0) | 高级配方合成台与多目标批量合成 |

---

- **2026-07-28**:
  - **【放置物品数量重置 BUG 彻底修复】**：
    - 排查并修复了玩家放置一组 64 个物品（如红石块）后手持数量被误重置为 1 个的严重 BUG。
    - 彻底移除 `MixinForgeHooksDebugHunter` 对 `ForgeHooks.getCount()` 的全局 `@Redirect` 拦截，以及 `DebugLogger` 和 `ItemDisplayBuilderMixin` 中对原始 `ItemStack` 对象做 `stack.setCount(1)` 的篡改代码。
    - 再次强化与践行 **【调试日志 MOD (DebugLogger) 专项规则】**，确保 DebugLogger 保持 100% 纯静默诊断，严禁写入任何玩法与逻辑修改。
- **2026-07-27**:
  - 确立并应用 **“调试日志仅作诊断查看”** 与 **“日志优先诊断原则（最高优先级）”** 铁律（持久化至 `.agents/AGENTS.md`）。
  - **【调试日志 (`DebugLogger`)】**：
    - 彻底清洗干涉 ProjectE 界面与过滤器的代码，恢复为纯静默诊断模式。
    - 新增 `CuriosDebugTracker` 槽位与命令专属诊断监听器，实现属性底层与 Capabilities 数据的实时捕获。
  - **【自定义等价交换 (`CustomEMC`)】**：
    - 恢复 `ProjectE` 官方原版镜像，修复 UTF-8 语言包转码。
    - 实现玩家进入/复活/跨维度时全自动解锁 ProjectE 全知识库 (`AutoKnowledgeHandler`)。
    - 新增 `MixinSlotTypeMessageBuilder` 字节码构建拦截，破解 IMC 硬编码限制，解封 *Flame Chase Artifacts (逐火十三英桀饰品)* 与 *Lightman's Currency* 的【货币战争饰品 (`currency_wars_curios` / `wallet`)】槽位上限至 15 个。
  - **【附魔等级上限突破 (`ELB`)】**：
    - 恢复 `EnchantingPlus` 100% 纯净原版镜像，将高级附魔台 0 经验消耗与全附魔解锁解除互斥完美移植至 `elb` 项目中。
- **2026-07-24**:
  - 全面移除 **【调试日志 (`DebugLogger`)】** 前台按键绑定与浮窗（完全纯后台全自动化运行，自动侦测并记录诊断报告）。
  - 重构 **【调试日志 (`DebugLogger`)】** 崩溃防护系统。
- **2026-07-23**:
  - 新增全新自研模组 **【定位物品-怪 (`ItemEntityTracker`)】**。
