package com.antigravity.devhelper.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class C2SUpdateAttributePacket {
    private final String attributeId;
    private final double newBaseValue;

    public C2SUpdateAttributePacket(String attributeId, double newBaseValue) {
        this.attributeId = attributeId;
        this.newBaseValue = newBaseValue;
    }

    public C2SUpdateAttributePacket(FriendlyByteBuf buf) {
        this.attributeId = buf.readUtf();
        this.newBaseValue = buf.readDouble();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.attributeId);
        buf.writeDouble(this.newBaseValue);
    }

    public void handle(Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ResourceLocation loc = ResourceLocation.tryParse(this.attributeId);
            if (loc != null && ForgeRegistries.ATTRIBUTES.containsKey(loc)) {
                Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(loc);
                if (attribute != null) {
                    AttributeInstance inst = player.getAttribute(attribute);
                    if (inst != null) {
                        double oldMaxHealth = player.getMaxHealth();
                        inst.setBaseValue(this.newBaseValue);

                        // 若修改的是最大生命值上限，同步拉满/适应当前血量
                        if (loc.toString().equals("minecraft:generic.max_health")) {
                            double newMaxHealth = player.getMaxHealth();
                            if (newMaxHealth > oldMaxHealth) {
                                player.setHealth(player.getHealth() + (float)(newMaxHealth - oldMaxHealth));
                            }
                        }
                    }
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
