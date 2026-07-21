package com.antigravity.quadhotbar.logic;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class HotbarManager {

    /**
     * 在服务端平滑轮换玩家的 4 层快捷栏
     * @param player 目标服务端玩家
     * @param direction +1 表示向下轮换下一层，-1 表示向上轮换上一层
     */
    public static void rotateHotbar(ServerPlayer player, int direction) {
        Inventory inv = player.getInventory();

        // 计算新的层级 Row (0, 1, 2, 3)
        // 假设我们以第一个物品或槽位数据做逻辑换行，每层 9 个槽位
        int currentLayer = getCurrentLayer(player);
        int nextLayer = (currentLayer + direction) % 4;
        if (nextLayer < 0) nextLayer += 4;

        if (currentLayer == nextLayer) return;

        // 将原版 Hotbar (0~8) 与 目标 Layer (9*nextLayer ~ 9*nextLayer + 8) 安全对调
        int targetOffset = nextLayer * 9;
        for (int i = 0; i < 9; i++) {
            int hotbarSlot = i;
            int targetSlot = targetOffset + i;

            ItemStack temp = inv.getItem(hotbarSlot);
            inv.setItem(hotbarSlot, inv.getItem(targetSlot));
            inv.setItem(targetSlot, temp);
        }

        setCurrentLayer(player, nextLayer);

        // 刷新容器广播与客户端同步
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
