package com.antigravity.devhelper.network;

import com.antigravity.devhelper.DevHelper;
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

            // 特殊处理 PMMO 技能 (如 pmmo:fishing, pmmo:combat 等)
            if (this.attributeId.startsWith("pmmo:")) {
                String skillName = this.attributeId.replace("pmmo:", "");
                if (player.getServer() != null) {
                    long val = (long) this.newBaseValue;
                    String pName = player.getScoreboardName();

                    // 兼容各种版本的 PMMO 指令语法格式
                    String[] cmds = {
                            "pmmo admin " + pName + " level set " + skillName + " " + val,
                            "pmmo admin " + pName + " set " + skillName + " level " + val,
                            "pmmo admin level set " + skillName + " " + val,
                            "pmmo admin set " + skillName + " level " + val
                    };

                    for (String cmd : cmds) {
                        try {
                            player.getServer().getCommands().performPrefixedCommand(
                                    player.createCommandSourceStack().withPermission(4).withSuppressedOutput(),
                                    cmd
                            );
                        } catch (Exception e) {
                            DevHelper.LOGGER.warn("[开发者辅助] PMMO 指令执行尝试失败: " + cmd, e);
                        }
                    }
                    DevHelper.LOGGER.info("[开发者辅助] 已成功提交 PMMO 技能改写请求: " + skillName + " -> " + val);
                }
                return;
            }

            // 处理原生与其它 MOD 的 Attribute 属性
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
