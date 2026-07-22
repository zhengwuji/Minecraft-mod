package com.antigravity.tracker.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.Map;

public class ChineseNameMapper {

    private static final Map<String, String> ALIAS_MAP = new HashMap<>();

    static {
        // 宝箱怪 (Mimic) 别名映射 (会咬人/攻击人的箱子怪物)
        ALIAS_MAP.put("artifacts:mimic", "宝箱怪 (会咬人的假宝箱)");
        ALIAS_MAP.put("faded_conquest_2:mimic", "惊骇宝箱怪 (会咬人的假宝箱)");
        ALIAS_MAP.put("grimoireofgaia:mimic", "虚伪宝箱怪 (盖亚宝箱怪)");
        ALIAS_MAP.put("aether:mimic", "以太宝箱怪 (以太假宝箱)");
        ALIAS_MAP.put("mowziesmobs:mimic", "密室宝箱怪 (Mowzie假宝箱)");
        ALIAS_MAP.put("alexsmobs:mimicube", "拟态拟方怪 (Alex假宝箱)");

        // 常用怪物别名
        ALIAS_MAP.put("minecraft:zombie", "僵尸");
        ALIAS_MAP.put("minecraft:skeleton", "骷髅");
        ALIAS_MAP.put("minecraft:creeper", "苦力怕 / 爬行者");
        ALIAS_MAP.put("minecraft:spider", "蜘蛛");
        ALIAS_MAP.put("minecraft:enderman", "末影人");
        ALIAS_MAP.put("minecraft:witch", "女巫");
        ALIAS_MAP.put("minecraft:ender_dragon", "末影龙");
        ALIAS_MAP.put("minecraft:wither", "凋灵");

        // 常用矿石/方块别名
        ALIAS_MAP.put("minecraft:diamond_ore", "钻石矿石");
        ALIAS_MAP.put("minecraft:deepslate_diamond_ore", "深片岩钻石矿石");
        ALIAS_MAP.put("minecraft:ancient_debris", "远古残骸");
        ALIAS_MAP.put("minecraft:chest", "普通静止宝箱 (非怪物)");
        ALIAS_MAP.put("minecraft:spawner", "刷怪笼");
    }

    public static String getEntityName(EntityType<?> type, String id) {
        if (ALIAS_MAP.containsKey(id)) return ALIAS_MAP.get(id);
        if (type == null) return cleanId(id);
        try {
            Component comp = Component.translatable(type.getDescriptionId());
            String text = comp.getString();
            if (!text.isEmpty() && !text.equals(type.getDescriptionId())) {
                return text;
            }
        } catch (Exception ignored) {}
        return cleanId(id);
    }

    public static String getBlockName(Block block, String id) {
        if (ALIAS_MAP.containsKey(id)) return ALIAS_MAP.get(id);
        if (block == null) return cleanId(id);
        try {
            Component comp = Component.translatable(block.getDescriptionId());
            String text = comp.getString();
            if (!text.isEmpty() && !text.equals(block.getDescriptionId())) {
                return text;
            }
        } catch (Exception ignored) {}
        return cleanId(id);
    }

    public static String getItemName(Item item, String id) {
        if (ALIAS_MAP.containsKey(id)) return ALIAS_MAP.get(id);
        if (item == null) return cleanId(id);
        try {
            Component comp = Component.translatable(item.getDescriptionId());
            String text = comp.getString();
            if (!text.isEmpty() && !text.equals(item.getDescriptionId())) {
                return text;
            }
        } catch (Exception ignored) {}
        return cleanId(id);
    }

    private static String cleanId(String id) {
        if (id == null) return "";
        String name = id.contains(":") ? id.split(":")[1] : id;
        return name.replace("_", " ");
    }
}
