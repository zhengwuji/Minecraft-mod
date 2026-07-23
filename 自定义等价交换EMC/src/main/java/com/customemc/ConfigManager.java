package com.customemc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = FMLPaths.CONFIGDIR.get().resolve("custom_emc.json").toFile();

    private static long defaultEMC = 5555L;
    private static final Map<String, Long> customEMCMap = new HashMap<>();

    public static void loadConfig() {
        customEMCMap.clear();
        defaultEMC = 5555L;

        try {
            if (!CONFIG_FILE.exists()) {
                createDefaultConfig();
            }

            try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                if (json.has("default_emc")) {
                    defaultEMC = json.get("default_emc").getAsLong();
                }

                if (json.has("custom_emc") && json.get("custom_emc").isJsonObject()) {
                    JsonObject customObj = json.getAsJsonObject("custom_emc");
                    for (String key : customObj.keySet()) {
                        try {
                            long val = customObj.get(key).getAsLong();
                            customEMCMap.put(key, val);
                        } catch (Exception e) {
                            LOGGER.warn("Invalid EMC value for item {} in custom_emc.json", key);
                        }
                    }
                }
            }
            LOGGER.info("[自定义等价交换EMC] 配置加载成功！默认EMC: {}, 自定义指定项: {}", defaultEMC, customEMCMap.size());
        } catch (Exception e) {
            LOGGER.error("[自定义等价交换EMC] 配置加载失败：", e);
        }
    }

    public static void saveConfig(long newDefaultEMC, Map<String, Long> newCustomMap) {
        defaultEMC = newDefaultEMC;
        customEMCMap.clear();
        if (newCustomMap != null) {
            customEMCMap.putAll(newCustomMap);
        }

        try {
            File dir = CONFIG_FILE.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }

            JsonObject json = new JsonObject();
            json.addProperty("default_emc", defaultEMC);

            JsonObject customObj = new JsonObject();
            for (Map.Entry<String, Long> entry : customEMCMap.entrySet()) {
                customObj.addProperty(entry.getKey(), entry.getValue());
            }
            json.add("custom_emc", customObj);

            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(json, writer);
            }
            LOGGER.info("[自定义等价交换EMC] 配置文件保存成功！路径: {}", CONFIG_FILE.getAbsolutePath());
        } catch (Exception e) {
            LOGGER.error("[自定义等价交换EMC] 配置文件保存失败：", e);
        }
    }

    public static void setDefaultEMC(long val) {
        saveConfig(val, customEMCMap);
    }

    public static void setCustomPrice(String itemId, long price) {
        customEMCMap.put(itemId, price);
        saveConfig(defaultEMC, customEMCMap);
    }

    private static void createDefaultConfig() {
        Map<String, Long> defaultMap = new HashMap<>();
        defaultMap.put("minecraft:dirt", 1L);
        defaultMap.put("minecraft:cobblestone", 1L);
        saveConfig(5555L, defaultMap);
    }

    public static long getDefaultEMC() {
        return defaultEMC;
    }

    public static Map<String, Long> getCustomEMCMap() {
        return customEMCMap;
    }
}
