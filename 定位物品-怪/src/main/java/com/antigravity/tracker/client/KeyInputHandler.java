package com.antigravity.tracker.client;

import com.antigravity.tracker.ItemEntityTracker;
import com.antigravity.tracker.client.gui.TrackerScreen;
import com.antigravity.tracker.config.TrackerConfig;
import com.antigravity.tracker.util.ChineseNameMapper;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ItemEntityTracker.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    public static final KeyMapping KEY_OPEN_TRACKER = new KeyMapping(
            "key.itementitytracker.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "key.categories.itementitytracker"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_OPEN_TRACKER);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (KEY_OPEN_TRACKER.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                Player player = mc.player;
                if (player == null) continue;

                if (Screen.hasShiftDown()) {
                    // Shift + F6 准星快捷锁定
                    HitResult hit = mc.hitResult;
                    if (hit instanceof EntityHitResult entityHit) {
                        Entity targetEntity = entityHit.getEntity();
                        ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(targetEntity.getType());
                        if (entityLoc != null) {
                            String eId = entityLoc.toString();
                            boolean isTracked = TrackerConfig.trackedEntities.containsKey(eId);
                            int color = TrackerConfig.getEntityColor(eId);
                            TrackerConfig.toggleEntity(eId, !isTracked, color);

                            String name = ChineseNameMapper.getEntityName(targetEntity.getType(), eId);
                            String msg = (!isTracked) ? "🎯 [定位物品-怪] 已开启追踪准星实体: " + name : "❌ [定位物品-怪] 已取消追踪实体: " + name;
                            player.displayClientMessage(Component.literal(msg), true);
                        }
                    } else if (hit instanceof BlockHitResult blockHit) {
                        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
                        Block block = state.getBlock();
                        ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                        if (bLoc != null) {
                            String bId = bLoc.toString();
                            boolean isTracked = TrackerConfig.trackedBlocks.containsKey(bId);
                            int color = TrackerConfig.getBlockColor(bId);
                            TrackerConfig.toggleBlock(bId, !isTracked, color);

                            String name = ChineseNameMapper.getBlockName(block, bId);
                            String msg = (!isTracked) ? "🎯 [定位物品-怪] 已开启追踪准星方块: " + name : "❌ [定位物品-怪] 已取消追踪方块: " + name;
                            player.displayClientMessage(Component.literal(msg), true);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("⚠️ [定位物品-怪] 请将准星对准实体或方块进行锁定"), true);
                    }
                } else {
                    if (mc.screen == null) {
                        mc.setScreen(new TrackerScreen());
                    }
                }
            }
        }
    }
}
