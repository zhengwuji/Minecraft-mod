package com.antigravity.foodbuffbag.network;

import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketQuickDeposit {
    public PacketQuickDeposit() {}

    public PacketQuickDeposit(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof FoodBuffMenu menu) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    boolean changed = false;
                    // 遍历玩家背包主槽位及快捷栏 (0 - 35)
                    for (int i = 0; i < player.getInventory().items.size(); i++) {
                        ItemStack pStack = player.getInventory().items.get(i);
                        if (!pStack.isEmpty() && (pStack.isEdible() || pStack.getItem().isEdible())) {
                            ItemStack remaining = depositToCap(cap, pStack);
                            if (remaining.getCount() != pStack.getCount()) {
                                player.getInventory().items.set(i, remaining);
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        menu.broadcastChanges();
                    }
                });
            }
        });
        ctx.setPacketHandled(true);
    }

    private ItemStack depositToCap(ItemStackHandler cap, ItemStack stack) {
        ItemStack toInsert = stack.copy();
        for (int slot = 0; slot < cap.getSlots(); slot++) {
            toInsert = cap.insertItem(slot, toInsert, false);
            if (toInsert.isEmpty()) {
                break;
            }
        }
        return toInsert;
    }
}
