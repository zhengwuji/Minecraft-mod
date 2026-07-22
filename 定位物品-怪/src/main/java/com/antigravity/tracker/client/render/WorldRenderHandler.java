package com.antigravity.tracker.client.render;

import com.antigravity.tracker.ItemEntityTracker;
import com.antigravity.tracker.config.TrackerConfig;
import com.antigravity.tracker.util.ChineseNameMapper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.Locale;

@Mod.EventBusSubscriber(modid = ItemEntityTracker.MODID, value = Dist.CLIENT)
public class WorldRenderHandler {

    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (!TrackerConfig.enabled) return;
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ClientLevel level = mc.level;
        if (player == null || level == null) return;

        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer linesConsumer = bufferSource.getBuffer(RenderType.lines());

        // 1. 扫描与渲染【实体/怪物/掉落物】
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == player) continue;
            double distSq = entity.distanceToSqr(player);
            if (distSq > TrackerConfig.maxDistance * TrackerConfig.maxDistance) continue;

            String trackerId = null;
            String name = "";
            int color = 0xFF00FF00;

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty()) {
                    ResourceLocation itemLoc = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemLoc != null && TrackerConfig.trackedItems.containsKey(itemLoc.toString())) {
                        trackerId = itemLoc.toString();
                        color = TrackerConfig.getItemColor(trackerId);
                        name = ChineseNameMapper.getItemName(stack.getItem(), trackerId);
                    }
                }
            } else {
                ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                if (entityLoc != null && TrackerConfig.trackedEntities.containsKey(entityLoc.toString())) {
                    trackerId = entityLoc.toString();
                    color = TrackerConfig.getEntityColor(trackerId);
                    name = ChineseNameMapper.getEntityName(entity.getType(), trackerId);
                }
            }

            if (trackerId != null) {
                double dist = Math.sqrt(distSq);
                AABB box = entity.getBoundingBox().move(-camPos.x, -camPos.y, -camPos.z);
                renderColorBox(poseStack, linesConsumer, box, color);

                if (TrackerConfig.showTracers) {
                    renderTracerLine(poseStack, linesConsumer, camPos, entity.position(), color);
                }

                if (TrackerConfig.showDistanceText) {
                    String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
                    renderBillboardText(poseStack, bufferSource, mc.font, text, entity.getX() - camPos.x, entity.getY() + entity.getBbHeight() + 0.3 - camPos.y, entity.getZ() - camPos.z, color);
                }
            }
        }

        // 2. 扫描与渲染【方块与矿石】(玩家周围 16x16 块区域扫描)
        if (!TrackerConfig.trackedBlocks.isEmpty()) {
            BlockPos pPos = player.blockPosition();
            int radius = (int) Math.min(TrackerConfig.maxDistance, 32); // 限制方块扫描半径
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        mutable.set(pPos.getX() + x, pPos.getY() + y, pPos.getZ() + z);
                        BlockState state = level.getBlockState(mutable);
                        if (state.isAir()) continue;

                        Block block = state.getBlock();
                        ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                        if (bLoc != null && TrackerConfig.trackedBlocks.containsKey(bLoc.toString())) {
                            String bId = bLoc.toString();
                            int color = TrackerConfig.getBlockColor(bId);
                            double dist = Math.sqrt(mutable.distSqr(pPos));

                            AABB box = new AABB(mutable).move(-camPos.x, -camPos.y, -camPos.z);
                            renderColorBox(poseStack, linesConsumer, box, color);

                            if (TrackerConfig.showDistanceText) {
                                String name = ChineseNameMapper.getBlockName(block, bId);
                                String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
                                renderBillboardText(poseStack, bufferSource, mc.font, text, mutable.getX() + 0.5 - camPos.x, mutable.getY() + 1.2 - camPos.y, mutable.getZ() + 0.5 - camPos.z, color);
                            }
                        }
                    }
                }
            }
        }

        bufferSource.endBatch(RenderType.lines());
    }

    private static void renderColorBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = 0.9F;

        LevelRenderer.renderLineBox(poseStack, consumer, box, r, g, b, a);
    }

    private static void renderTracerLine(PoseStack poseStack, VertexConsumer consumer, Vec3 camPos, Vec3 targetPos, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        // 视线中心到目标中心
        Vec3 start = new Vec3(0, 0.2, 0.5);
        Vec3 end = targetPos.subtract(camPos);

        consumer.vertex(mat, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, 0.8f).normal(pose.normal(), 0, 1, 0).endVertex();
        consumer.vertex(mat, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, 0.8f).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    private static void renderBillboardText(PoseStack poseStack, MultiBufferSource bufferSource, Font font, String text, double x, double y, double z, int color) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        Matrix4f matrix4f = poseStack.last().pose();
        float textWidth = font.width(text) / 2.0F;

        RenderSystem.disableDepthTest();
        font.drawInBatch(text, -textWidth, 0, color, false, matrix4f, bufferSource, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }
}
