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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
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

        // 2. 高性能 X-Ray 级全区块方块与宝箱扫描器
        if (!TrackerConfig.trackedBlocks.isEmpty()) {
            scanAndRenderXRayBlocks(level, player, camPos, poseStack, linesConsumer, bufferSource, mc.font);
        }

        bufferSource.endBatch(RenderType.lines());
        RenderSystem.enableDepthTest();
        RenderSystem.lineWidth(1.0F);
    }

    private static void scanAndRenderXRayBlocks(ClientLevel level, Player player, Vec3 camPos, PoseStack poseStack, VertexConsumer linesConsumer, MultiBufferSource.BufferSource bufferSource, Font font) {
        BlockPos pPos = player.blockPosition();
        int pChunkX = pPos.getX() >> 4;
        int pChunkZ = pPos.getZ() >> 4;
        int chunkRadius = (int) Math.min(Math.ceil(TrackerConfig.maxDistance / 16.0), 16);

        Set<BlockPos> renderedPositions = new HashSet<>();

        for (int cx = pChunkX - chunkRadius; cx <= pChunkX + chunkRadius; cx++) {
            for (int cz = pChunkZ - chunkRadius; cz <= pChunkZ + chunkRadius; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                // A. 扫描 BlockEntity (全模组箱子、Lootr战利品箱、木桶、刷怪笼、精妙背包等，100%覆盖)
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos bPos = entry.getKey();
                    double distSq = bPos.distSqr(pPos);
                    if (distSq > TrackerConfig.maxDistance * TrackerConfig.maxDistance) continue;

                    BlockState state = entry.getValue().getBlockState();
                    Block block = state.getBlock();
                    ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                    if (bLoc != null) {
                        String bId = bLoc.toString();
                        if (isBlockTracked(bId, block, state)) {
                            renderedPositions.add(bPos);
                            renderBlockTarget(poseStack, linesConsumer, bufferSource, font, camPos, bPos, block, bId, Math.sqrt(distSq));
                        }
                    }
                }

                // B. 扫描区块 Sub-Sections (普通钻石矿石、远古残骸、非 BlockEntity 箱子)
                LevelChunkSection[] sections = chunk.getSections();
                for (int sIdx = 0; sIdx < sections.length; sIdx++) {
                    LevelChunkSection section = sections[sIdx];
                    if (section == null || section.hasOnlyAir()) continue;

                    int sectionMinY = level.getMinBuildHeight() + sIdx * 16;
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                BlockState state = section.getBlockState(x, y, z);
                                if (state.isAir()) continue;

                                Block block = state.getBlock();
                                ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                                if (bLoc != null) {
                                    String bId = bLoc.toString();
                                    if (isBlockTracked(bId, block, state)) {
                                        BlockPos bPos = new BlockPos((cx << 4) + x, sectionMinY + y, (cz << 4) + z);
                                        if (renderedPositions.add(bPos)) {
                                            double distSq = bPos.distSqr(pPos);
                                            if (distSq <= TrackerConfig.maxDistance * TrackerConfig.maxDistance) {
                                                renderBlockTarget(poseStack, linesConsumer, bufferSource, font, camPos, bPos, block, bId, Math.sqrt(distSq));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void renderBlockTarget(PoseStack poseStack, VertexConsumer linesConsumer, MultiBufferSource.BufferSource bufferSource, Font font, Vec3 camPos, BlockPos bPos, Block block, String bId, double dist) {
        int color = TrackerConfig.getBlockColor(bId);

        AABB box = new AABB(bPos).move(-camPos.x, -camPos.y, -camPos.z);
        renderColorBox(poseStack, linesConsumer, box, color);

        if (TrackerConfig.showTracers) {
            renderTracerLine(poseStack, linesConsumer, camPos, new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5), color);
        }

        if (TrackerConfig.showDistanceText) {
            String name = ChineseNameMapper.getBlockName(block, bId);
            String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
            renderBillboardText(poseStack, bufferSource, font, text, bPos.getX() + 0.5 - camPos.x, bPos.getY() + 1.2 - camPos.y, bPos.getZ() + 0.5 - camPos.z, color);
        }
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

    public static boolean isBlockTracked(String bId, Block block, BlockState state) {
        if (TrackerConfig.trackedBlocks.containsKey(bId)) return true;

        String lowerId = bId.toLowerCase(Locale.ROOT);

        // 如果开启了任何箱子/宝箱相关的追查目标
        boolean chestTracked = false;
        for (String key : TrackerConfig.trackedBlocks.keySet()) {
            String lKey = key.toLowerCase(Locale.ROOT);
            if (lKey.contains("chest") || lKey.contains("barrel") || lKey.contains("box") || lKey.contains("storage") || lKey.contains("箱子") || lKey.contains("宝箱")) {
                chestTracked = true;
                break;
            }
        }

        if (chestTracked) {
            if (lowerId.contains("chest") || lowerId.contains("barrel") || lowerId.contains("shulker") || lowerId.contains("lootr") || lowerId.contains("storage") || lowerId.contains("crate") || lowerId.contains("vault")) {
                return true;
            }
            if (block instanceof AbstractChestBlock || block instanceof ChestBlock || block instanceof EnderChestBlock || block instanceof BarrelBlock || block instanceof ShulkerBoxBlock) {
                return true;
            }
        }

        // 刷怪笼通配
        boolean spawnerTracked = false;
        for (String key : TrackerConfig.trackedBlocks.keySet()) {
            if (key.contains("spawner")) {
                spawnerTracked = true;
                break;
            }
        }
        if (spawnerTracked && lowerId.contains("spawner")) return true;

        // 矿石通配
        boolean oreTracked = false;
        for (String key : TrackerConfig.trackedBlocks.keySet()) {
            if (key.contains("ore") || key.contains("debris")) {
                oreTracked = true;
                break;
            }
        }
        if (oreTracked && (lowerId.contains("ore") || lowerId.contains("debris"))) return true;

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
