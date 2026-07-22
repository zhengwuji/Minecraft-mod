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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import java.util.Set;

@Mod.EventBusSubscriber(modid = ItemEntityTracker.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    public static final KeyMapping KEY_OPEN_TRACKER = new KeyMapping(
            "key.itementitytracker.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "key.categories.itementitytracker"
    );

    // 常见自然地表环境方块保护列表 (防止 Shift+F6 误触脚下泥土/石头导致全图地形发光)
    private static final Set<String> COMMON_TERRAIN_BLOCKS = Set.of(
            "minecraft:grass_block", "minecraft:dirt", "minecraft:coarse_dirt", "minecraft:podzol",
            "minecraft:stone", "minecraft:granite", "minecraft:diorite", "minecraft:andesite",
            "minecraft:deepslate", "minecraft:tuff", "minecraft:sand", "minecraft:red_sand",
            "minecraft:gravel", "minecraft:water", "minecraft:lava", "minecraft:bedrock",
            "minecraft:netherrack", "minecraft:end_stone", "minecraft:basalt", "minecraft:blackstone",
            "minecraft:oak_leaves", "minecraft:spruce_leaves", "minecraft:birch_leaves",
            "minecraft:jungle_leaves", "minecraft:acacia_leaves", "minecraft:dark_oak_leaves"
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
                if (player == null || mc.level == null) continue;

                if (Screen.hasShiftDown()) {
                    // 长距离高精度视线检测 (最长 64 米)
                    double reach = Math.min(TrackerConfig.maxDistance, 64.0);
                    Vec3 eyePos = player.getEyePosition(1.0F);
                    Vec3 viewVec = player.getViewVector(1.0F);
                    Vec3 endPos = eyePos.add(viewVec.scale(reach));

                    // 1. 优先检测视线上的实体/怪物/掉落物
                    AABB searchBox = player.getBoundingBox().expandTowards(viewVec.scale(reach)).inflate(1.0);
                    EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                            player, eyePos, endPos, searchBox,
                            e -> !e.isSpectator() && e.isPickable() && e != player,
                            reach * reach
                    );

                    if (entityHit != null) {
                        Entity targetEntity = entityHit.getEntity();
                        ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(targetEntity.getType());
                        if (entityLoc != null) {
                            String eId = entityLoc.toString();
                            boolean isTracked = TrackerConfig.trackedEntities.containsKey(eId);
                            int color = TrackerConfig.getEntityColor(eId);
                            TrackerConfig.toggleEntity(eId, !isTracked, color);

                            String name = ChineseNameMapper.getEntityName(targetEntity.getType(), eId);
                            String msg = (!isTracked) ? "🎯 [定位物品-怪] 已开启追踪实体: " + name : "❌ [定位物品-怪] 已取消追踪实体: " + name;
                            player.displayClientMessage(Component.literal(msg), true);
                        }
                        continue;
                    }

                    // 2. 检测视线上的方块
                    BlockHitResult blockHit = mc.level.clip(new ClipContext(
                            eyePos, endPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
                    ));

                    if (blockHit.getType() == HitResult.Type.BLOCK) {
                        BlockState state = mc.level.getBlockState(blockHit.getBlockPos());
                        Block block = state.getBlock();
                        ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                        if (bLoc != null) {
                            String bId = bLoc.toString();
                            String name = ChineseNameMapper.getBlockName(block, bId);

                            // 如果是草方块/泥土/石头等常见环境方块，进行防误触拦截
                            if (COMMON_TERRAIN_BLOCKS.contains(bId)) {
                                String msg = String.format("⚠️ [定位物品-怪] 已忽略自然地表方块 (%s)，防止全图高亮！如需追踪请在 F6 面板搜索", name);
                                player.displayClientMessage(Component.literal(msg), true);
                                continue;
                            }

                            boolean isTracked = TrackerConfig.trackedBlocks.containsKey(bId);
                            int color = TrackerConfig.getBlockColor(bId);
                            TrackerConfig.toggleBlock(bId, !isTracked, color);

                            String msg = (!isTracked) ? "🎯 [定位物品-怪] 已开启追踪方块: " + name : "❌ [定位物品-怪] 已取消追踪方块: " + name;
                            player.displayClientMessage(Component.literal(msg), true);
                        }
                    } else {
                        player.displayClientMessage(Component.literal("⚠️ [定位物品-怪] 未瞄准到有效实体或特殊方块"), true);
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
