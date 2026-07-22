package com.antigravity.tracker.config;

import java.util.HashMap;
import java.util.Map;

public class TrackerConfig {
    public static boolean enabled = true;
    public static double maxDistance = 1000.0; // 默认 1000 米无限制
    public static boolean showTracers = true;
    public static boolean showDistanceText = true;

    // 目标注册名 ID -> ARGB 颜色值映射
    public static final Map<String, Integer> trackedEntities = new HashMap<>();
    public static final Map<String, Integer> trackedBlocks = new HashMap<>();
    public static final Map<String, Integer> trackedItems = new HashMap<>();

    // 8 种高亮色彩预设 (ARGB)
    public static final int[] PRESET_COLORS = {
            0xFFFF0000, // 🔴 红色
            0xFF00FF00, // 🟢 绿色
            0xFF0088FF, // 🔵 蓝色
            0xFFFFFF00, // 🟡 黄色
            0xFFBB00FF, // 🟣 紫色
            0xFF00FFFF, // 🩵 青色
            0xFFFF8800, // 🟠 橙色
            0xFFFFFFFF  // ⚪ 白色
    };

    public static final String[] PRESET_COLOR_NAMES = {
            "🔴 红色", "🟢 绿色", "🔵 蓝色", "🟡 黄色", "🟣 紫色", "🩵 青色", "🟠 橙色", "⚪ 白色"
    };

    static {
        // 默认初始化预设常用目标
        trackedEntities.put("artifacts:mimic", 0xFFFF0000);         // 宝箱怪 -> 红色
        trackedEntities.put("faded_conquest_2:mimic", 0xFFFF0000);   // 惊骇宝箱怪 -> 红色
        trackedEntities.put("grimoireofgaia:mimic", 0xFFFF0000);     // 虚伪宝箱怪 -> 红色
        trackedEntities.put("minecraft:zombie", 0xFFFF0000);         // 僵尸 -> 红色
        trackedEntities.put("minecraft:skeleton", 0xFFFF0000);       // 骷髅 -> 红色
        trackedEntities.put("minecraft:creeper", 0xFFFF8800);        // 苦力怕 -> 橙色
        trackedEntities.put("minecraft:ender_dragon", 0xFFBB00FF);    // 末影龙 -> 紫色

        trackedBlocks.put("minecraft:diamond_ore", 0xFF00FFFF);       // 钻石矿石 -> 青色
        trackedBlocks.put("minecraft:deepslate_diamond_ore", 0xFF00FFFF);
        trackedBlocks.put("minecraft:ancient_debris", 0xFFFFFF00);    // 远古残骸 -> 黄色
        trackedBlocks.put("minecraft:chest", 0xFFFFFF00);             // 宝箱 -> 黄色

        trackedItems.put("minecraft:diamond", 0xFF00FFFF);           // 钻石掉落物 -> 青色
        trackedItems.put("minecraft:netherite_ingot", 0xFFBB00FF);   // 下界合金锭 -> 紫色
    }

    public static int getEntityColor(String id) {
        return trackedEntities.getOrDefault(id, 0xFF00FF00);
    }

    public static int getBlockColor(String id) {
        return trackedBlocks.getOrDefault(id, 0xFFFFFF00);
    }

    public static int getItemColor(String id) {
        return trackedItems.getOrDefault(id, 0xFF00FFFF);
    }

    public static void toggleEntity(String id, boolean track, int color) {
        if (track) {
            trackedEntities.put(id, color);
        } else {
            trackedEntities.remove(id);
        }
    }

    public static void toggleBlock(String id, boolean track, int color) {
        if (track) {
            trackedBlocks.put(id, color);
        } else {
            trackedBlocks.remove(id);
        }
    }

    public static void toggleItem(String id, boolean track, int color) {
        if (track) {
            trackedItems.put(id, color);
        } else {
            trackedItems.remove(id);
        }
    }

    public static int getNextColor(int currentColor) {
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            if (PRESET_COLORS[i] == currentColor) {
                return PRESET_COLORS[(i + 1) % PRESET_COLORS.length];
            }
        }
        return PRESET_COLORS[0];
    }
}
