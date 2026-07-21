package com.antigravity.quadhotbar.network;

import com.antigravity.quadhotbar.logic.HotbarManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketSwitchHotbar {
    private final int direction; // +1 下一页 / -1 上一页

    public PacketSwitchHotbar(int direction) {
        this.direction = direction;
    }

    public PacketSwitchHotbar(FriendlyByteBuf buf) {
        this.direction = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.direction);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                HotbarManager.rotateHotbar(player, this.direction);
            }
        });
        ctx.setPacketHandled(true);
    }
}
