package com.customemc;

import moze_intel.projecte.api.capabilities.PECapabilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomEMCMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AutoKnowledgeHandler {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        grantFullKnowledge(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        grantFullKnowledge(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        grantFullKnowledge(event.getEntity());
    }

    @SubscribeEvent
    public static void onOpenContainer(PlayerContainerEvent.Open event) {
        grantFullKnowledge(event.getEntity());
    }

    public static void grantFullKnowledge(Player playerEntity) {
        if (playerEntity instanceof ServerPlayer player) {
            try {
                player.getCapability(PECapabilities.KNOWLEDGE_CAPABILITY).ifPresent(provider -> {
                    if (!provider.hasFullKnowledge()) {
                        provider.setFullKnowledge(true);
                        provider.sync(player);
                        CustomEMCMod.LOGGER.info("[自定义等价交换EMC] 已为玩家 {} 自动解锁全知识库！", player.getScoreboardName());
                    }
                });
            } catch (Throwable t) {
                CustomEMCMod.LOGGER.error("[自定义等价交换EMC] 自动解锁全知识库失败", t);
            }
        }
    }
}
