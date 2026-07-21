package com.antigravity.quadhotbar.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class HotbarManager {

    /**
     * 在服务端平滑轮换玩家的快捷栏 (4 层 / 36 槽位)
     */
    public static void rotateHotbar(ServerPlayer player, int direction) {
        Inventory inv = player.getInventory();

        int currentLayer = getCurrentLayer(player);
        int nextLayer = (currentLayer + direction) % 4;
        if (nextLayer < 0) nextLayer += 4;

        if (currentLayer == nextLayer) return;

        // 对调原版快捷栏 (Slot 0~8) 与 目标 Layer (9*nextLayer ~ 9*nextLayer + 8)
        int targetOffset = nextLayer * 9;
        for (int i = 0; i < 9; i++) {
            int hotbarSlot = i;
            int targetSlot = targetOffset + i;

            ItemStack temp = inv.getItem(hotbarSlot);
            inv.setItem(hotbarSlot, inv.getItem(targetSlot));
            inv.setItem(targetSlot, temp);
        }

        setCurrentLayer(player, nextLayer);

        player.containerMenu.broadcastChanges();

        int startSlot = nextLayer * 9 + 1;
        int endSlot = nextLayer * 9 + 9;
        player.displayClientMessage(
                Component.translatable("msg.quadhotbar.switched", (nextLayer + 1), startSlot, endSlot),
                true
        );
    }

    private static int getCurrentLayer(ServerPlayer player) {
        return player.getPersistentData().getInt("QuadHotbarCurrentLayer");
    }

    private static void setCurrentLayer(ServerPlayer player, int layer) {
        player.getPersistentData().putInt("QuadHotbarCurrentLayer", layer);
    }
}
