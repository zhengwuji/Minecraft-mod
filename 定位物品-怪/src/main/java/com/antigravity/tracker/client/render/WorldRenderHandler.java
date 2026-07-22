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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

        RenderSystem.disableDepthTest();
        RenderSystem.lineWidth(3.0F);

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
                    if (itemLoc != null && isItemTracked(itemLoc.toString())) {
                        trackerId = itemLoc.toString();
                        color = TrackerConfig.getItemColor(trackerId);
                        name = ChineseNameMapper.getItemName(stack.getItem(), trackerId);
                    }
                }
            } else {
                ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                if (entityLoc != null && isEntityTracked(entityLoc.toString())) {
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
                    renderTracerLine(poseStack, linesConsumer, camPos, entity.getBoundingBox().getCenter(), color);
                }

                if (TrackerConfig.showDistanceText) {
                    String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
                    renderBillboardText(poseStack, bufferSource, mc.font, text, entity.getX() - camPos.x, entity.getY() + entity.getBbHeight() + 0.4 - camPos.y, entity.getZ() - camPos.z, color);
                }
            }
        }

        // 2. 扫描与渲染【方块/宝箱/刷怪笼/矿石】 (全区块方块实体 + 近场矿石快速检索)
        if (!TrackerConfig.trackedBlocks.isEmpty()) {
            BlockPos pPos = player.blockPosition();
            int pChunkX = pPos.getX() >> 4;
            int pChunkZ = pPos.getZ() >> 4;
            int chunkRadius = (int) Math.min(Math.ceil(TrackerConfig.maxDistance / 16.0), 16);

            Set<BlockPos> renderedPositions = new HashSet<>();

            // A. 区块 BlockEntity 扫描 (宝箱、Lootr、刷怪笼、容器等，效率极高且 100% 涵盖全地图加载箱子)
            for (int cx = pChunkX - chunkRadius; cx <= pChunkX + chunkRadius; cx++) {
                for (int cz = pChunkZ - chunkRadius; cz <= pChunkZ + chunkRadius; cz++) {
                    LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
                    if (chunk == null) continue;

                    for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                        BlockPos bPos = entry.getKey();
                        double distSq = bPos.distSqr(pPos);
                        if (distSq > TrackerConfig.maxDistance * TrackerConfig.maxDistance) continue;

                        BlockState state = entry.getValue().getBlockState();
                        Block block = state.getBlock();
                        ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                        if (bLoc != null) {
                            String bId = bLoc.toString();
                            if (isBlockTracked(bId)) {
                                renderedPositions.add(bPos);
                                int color = TrackerConfig.getBlockColor(bId);
                                double dist = Math.sqrt(distSq);

                                AABB box = new AABB(bPos).move(-camPos.x, -camPos.y, -camPos.z);
                                renderColorBox(poseStack, linesConsumer, box, color);

                                if (TrackerConfig.showTracers) {
                                    renderTracerLine(poseStack, linesConsumer, camPos, new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5), color);
                                }

                                if (TrackerConfig.showDistanceText) {
                                    String name = ChineseNameMapper.getBlockName(block, bId);
                                    String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
                                    renderBillboardText(poseStack, bufferSource, mc.font, text, bPos.getX() + 0.5 - camPos.x, bPos.getY() + 1.2 - camPos.y, bPos.getZ() + 0.5 - camPos.z, color);
                                }
                            }
                        }
                    }
                }
            }

            // B. 普通矿石块扫描 (对于非 BlockEntity 的普通钻石矿石、古物残骸)
            int oreRadius = (int) Math.min(TrackerConfig.maxDistance, 48);
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

            for (int x = -oreRadius; x <= oreRadius; x += 1) {
                for (int y = -oreRadius; y <= oreRadius; y += 1) {
                    for (int z = -oreRadius; z <= oreRadius; z += 1) {
                        mutable.set(pPos.getX() + x, pPos.getY() + y, pPos.getZ() + z);
                        if (renderedPositions.contains(mutable)) continue;

                        BlockState state = level.getBlockState(mutable);
                        if (state.isAir()) continue;

                        Block block = state.getBlock();
                        ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                        if (bLoc != null) {
                            String bId = bLoc.toString();
                            if (isBlockTracked(bId)) {
                                int color = TrackerConfig.getBlockColor(bId);
                                double dist = Math.sqrt(mutable.distSqr(pPos));

                                AABB box = new AABB(mutable).move(-camPos.x, -camPos.y, -camPos.z);
                                renderColorBox(poseStack, linesConsumer, box, color);

                                if (TrackerConfig.showTracers) {
                                    renderTracerLine(poseStack, linesConsumer, camPos, new Vec3(mutable.getX() + 0.5, mutable.getY() + 0.5, mutable.getZ() + 0.5), color);
                                }

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
        }

        bufferSource.endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0F);
    }

    private static boolean isEntityTracked(String id) {
        if (TrackerConfig.trackedEntities.containsKey(id)) return true;
        if (TrackerConfig.trackedEntities.containsKey("minecraft:zombie")) {
            if (id.contains("mimic") || id.contains("zombie") || id.contains("skeleton") || id.contains("creeper")) return true;
        }
        return false;
    }

    private static boolean isItemTracked(String id) {
        return TrackerConfig.trackedItems.containsKey(id);
    }

    public static boolean isBlockTracked(String bId) {
        if (TrackerConfig.trackedBlocks.containsKey(bId)) return true;

        // 如果开启了宝箱类目标，通配全模组宝箱/箱子/木桶/战利品箱
        if (TrackerConfig.trackedBlocks.containsKey("minecraft:chest")) {
            if (bId.contains("chest") || bId.contains("barrel") || bId.contains("shulker") || bId.contains("lootr") || bId.contains("storage")) {
                return true;
            }
        }
        // 如果开启了刷怪笼目标，通配全模组刷怪笼
        if (TrackerConfig.trackedBlocks.containsKey("minecraft:spawner")) {
            if (bId.contains("spawner")) return true;
        }
        // 如果开启了矿石目标，通配矿石与残骸
        if (TrackerConfig.trackedBlocks.containsKey("minecraft:diamond_ore")) {
            if (bId.contains("ore") || bId.contains("debris")) return true;
        }
        return false;
    }

    private static void renderColorBox(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = 1.0F;

        LevelRenderer.renderLineBox(poseStack, consumer, box, r, g, b, a);
    }

    private static void renderTracerLine(PoseStack poseStack, VertexConsumer consumer, Vec3 camPos, Vec3 targetCenter, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        PoseStack.Pose pose = poseStack.last();
        Matrix4f mat = pose.pose();

        Vec3 start = new Vec3(0, 0, 0);
        Vec3 end = targetCenter.subtract(camPos);

        consumer.vertex(mat, (float) start.x, (float) start.y, (float) start.z).color(r, g, b, 1.0f).normal(pose.normal(), 0, 1, 0).endVertex();
        consumer.vertex(mat, (float) end.x, (float) end.y, (float) end.z).color(r, g, b, 1.0f).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    private static void renderBillboardText(PoseStack poseStack, MultiBufferSource bufferSource, Font font, String text, double x, double y, double z, int color) {
        Minecraft mc = Minecraft.getInstance();
        EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(dispatcher.cameraOrientation());
        poseStack.scale(-0.03F, -0.03F, 0.03F);

        Matrix4f matrix4f = poseStack.last().pose();
        float textWidth = font.width(text) / 2.0F;

        RenderSystem.disableDepthTest();
        font.drawInBatch(text, -textWidth, 0, color, false, matrix4f, bufferSource, Font.DisplayMode.SEE_THROUGH, 0, 0xF000F0);
        RenderSystem.enableDepthTest();

        poseStack.popPose();
    }
}
