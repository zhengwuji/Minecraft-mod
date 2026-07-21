package com.antigravity.foodbuffbag.network;

import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PacketChangePage {
    private final int pageDelta;

    public PacketChangePage(int pageDelta) {
        this.pageDelta = pageDelta;
    }

    public PacketChangePage(FriendlyByteBuf buf) {
        this.pageDelta = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.pageDelta);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null && player.containerMenu instanceof FoodBuffMenu menu) {
                menu.changePage(this.pageDelta);
            }
        });
        ctx.setPacketHandled(true);
    }
}
