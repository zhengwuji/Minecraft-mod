package com.antigravity.devhelper.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SUpdatePlayerStatsPacket {
    public enum Action {
        SET_HEALTH,
        SET_FOOD,
        SET_XP_LEVEL,
        HEAL_FULL
    }

    private final Action action;
    private final float value;

    public C2SUpdatePlayerStatsPacket(Action action, float value) {
        this.action = action;
        this.value = value;
    }

    public C2SUpdatePlayerStatsPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        this.value = buf.readFloat();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.action);
        buf.writeFloat(this.value);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            switch (this.action) {
                case SET_HEALTH -> player.setHealth(Math.min(this.value, player.getMaxHealth()));
                case SET_FOOD -> player.getFoodData().setFoodLevel((int) this.value);
                case SET_XP_LEVEL -> {
                    player.experienceLevel = (int) this.value;
                    player.experienceProgress = 0.0F;
                    player.totalExperience = 0;
                }
                case HEAL_FULL -> {
                    player.setHealth(player.getMaxHealth());
                    player.getFoodData().setFoodLevel(20);
                    player.getFoodData().setSaturation(20.0F);
                    player.clearFire();
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
