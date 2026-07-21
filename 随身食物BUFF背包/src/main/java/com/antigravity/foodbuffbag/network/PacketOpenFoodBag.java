package com.antigravity.foodbuffbag.network;

import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;

import java.util.function.Supplier;

public class PacketOpenFoodBag {
    public PacketOpenFoodBag() {}

    public PacketOpenFoodBag(FriendlyByteBuf buf) {}

    public void toBytes(FriendlyByteBuf buf) {}

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    NetworkHooks.openScreen(player, new SimpleMenuProvider(
                            (containerId, playerInventory, p) -> new FoodBuffMenu(containerId, playerInventory, cap),
                            Component.translatable("container.foodbuffbag.food_bag")
                    ));
                });
            }
        });
        ctx.setPacketHandled(true);
    }
}
