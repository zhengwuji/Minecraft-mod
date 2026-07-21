package com.tfar.anviltweaks.network;

import com.tfar.anviltweaks.RepairContainerv2;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketAnvilRename {
    private final String name;

    public CPacketAnvilRename(String name) {
        this.name = name;
    }

    public static void encode(CPacketAnvilRename msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name);
    }

    public static CPacketAnvilRename decode(FriendlyByteBuf buf) {
        return new CPacketAnvilRename(buf.readUtf(32767));
    }

    public static void handle(CPacketAnvilRename msg, Supplier<NetworkEvent.Context> ctxGetter) {
        NetworkEvent.Context ctx = ctxGetter.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof RepairContainerv2 menu) {
                menu.setItemName(msg.name);
            }
        });
        ctx.setPacketHandled(true);
    }
}
