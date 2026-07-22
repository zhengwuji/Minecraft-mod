package com.antigravity.tracker.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ChineseNameMapper {

    public static String getEntityName(EntityType<?> type, String id) {
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
