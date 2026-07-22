package com.antigravity.tracker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class TrackerConfig {
    // 每次进入游戏时，全局主开关强制保持关闭状态 (enabled = false)，防止进入游戏瞬间扫描造成卡顿
    public static boolean enabled = false;
    public static double maxDistance = 512.0; // 默认 512 米
    public static boolean showTracers = true;
    public static boolean showDistanceText = true;

    // 目标注册名 ID -> ARGB 颜色值映射 (保存用户的追踪配置)
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

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        loadConfig();
    }

    public static File getConfigFile() {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            return configPath.resolve("itementitytracker-client.json").toFile();
        } catch (Exception e) {
            return new File("config/itementitytracker-client.json");
        }
    }

    public static synchronized void loadConfig() {
        // 每次进入游戏加载配置时，主开关强制置为 false (保证进入游戏 0 卡顿)
        enabled = false;

        File file = getConfigFile();
        if (!file.exists()) return;

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            if (json.has("maxDistance")) maxDistance = json.get("maxDistance").getAsDouble();
            if (json.has("showTracers")) showTracers = json.get("showTracers").getAsBoolean();
            if (json.has("showDistanceText")) showDistanceText = json.get("showDistanceText").getAsBoolean();

            if (json.has("trackedEntities")) {
                trackedEntities.clear();
                JsonObject obj = json.getAsJsonObject("trackedEntities");
                obj.entrySet().forEach(entry -> trackedEntities.put(entry.getKey(), entry.getValue().getAsInt()));
            }

            if (json.has("trackedBlocks")) {
                trackedBlocks.clear();
                JsonObject obj = json.getAsJsonObject("trackedBlocks");
                obj.entrySet().forEach(entry -> trackedBlocks.put(entry.getKey(), entry.getValue().getAsInt()));
            }

            if (json.has("trackedItems")) {
                trackedItems.clear();
                JsonObject obj = json.getAsJsonObject("trackedItems");
                obj.entrySet().forEach(entry -> trackedItems.put(entry.getKey(), entry.getValue().getAsInt()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized void saveConfig() {
        try {
            File file = getConfigFile();
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            JsonObject json = new JsonObject();
            json.addProperty("enabled", enabled);
            json.addProperty("maxDistance", maxDistance);
            json.addProperty("showTracers", showTracers);
            json.addProperty("showDistanceText", showDistanceText);

            JsonObject entitiesObj = new JsonObject();
            trackedEntities.forEach(entitiesObj::addProperty);
            json.add("trackedEntities", entitiesObj);

            JsonObject blocksObj = new JsonObject();
            trackedBlocks.forEach(blocksObj::addProperty);
            json.add("trackedBlocks", blocksObj);

            JsonObject itemsObj = new JsonObject();
            trackedItems.forEach(itemsObj::addProperty);
            json.add("trackedItems", itemsObj);

            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        saveConfig();
    }

    public static void toggleBlock(String id, boolean track, int color) {
        if (track) {
            trackedBlocks.put(id, color);
        } else {
            trackedBlocks.remove(id);
        }
        saveConfig();
    }

    public static void toggleItem(String id, boolean track, int color) {
        if (track) {
            trackedItems.put(id, color);
        } else {
            trackedItems.remove(id);
        }
        saveConfig();
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
