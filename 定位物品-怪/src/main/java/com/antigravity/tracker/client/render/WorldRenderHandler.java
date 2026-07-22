package com.antigravity.tracker.client.render;

import com.antigravity.tracker.ItemEntityTracker;
import com.antigravity.tracker.config.TrackerConfig;
import com.antigravity.tracker.util.ChineseNameMapper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
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
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = ItemEntityTracker.MODID, value = Dist.CLIENT)
public class WorldRenderHandler {

    public static class BlockTarget {
        public final BlockPos pos;
        public final Block block;
        public final String id;

        public BlockTarget(BlockPos pos, Block block, String id) {
            this.pos = pos;
            this.block = block;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BlockTarget that = (BlockTarget) o;
            return Objects.equals(pos, that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    private static final List<BlockTarget> CACHED_BLOCK_TARGETS = new ArrayList<>();
    private static boolean isScanning = false;
    private static int tickCounter = 0;

    public static void clearCache() {
        synchronized (CACHED_BLOCK_TARGETS) {
            CACHED_BLOCK_TARGETS.clear();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!TrackerConfig.enabled || TrackerConfig.trackedBlocks.isEmpty()) {
            clearCache();
            return;
        }

        tickCounter++;
        if (tickCounter % 10 == 0 && !isScanning) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            ClientLevel level = mc.level;
            if (player != null && level != null) {
                isScanning = true;
                BlockPos pPos = player.blockPosition();
                double maxDist = TrackerConfig.maxDistance;

                CompletableFuture.runAsync(() -> {
                    try {
                        List<BlockTarget> newTargets = new ArrayList<>();
                        Set<BlockPos> foundPositions = new HashSet<>();

                        int pChunkX = pPos.getX() >> 4;
                        int pChunkZ = pPos.getZ() >> 4;
                        int chunkRadius = (int) Math.min(Math.ceil(maxDist / 16.0), 16);

                        for (int cx = pChunkX - chunkRadius; cx <= pChunkX + chunkRadius; cx++) {
                            for (int cz = pChunkZ - chunkRadius; cz <= pChunkZ + chunkRadius; cz++) {
                                LevelChunk chunk = level.getChunkSource().getChunk(cx, cz, false);
                                if (chunk == null) continue;

                                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                                    BlockPos bPos = entry.getKey();
                                    if (bPos.distSqr(pPos) > maxDist * maxDist) continue;

                                    BlockState state = entry.getValue().getBlockState();
                                    Block block = state.getBlock();
                                    ResourceLocation bLoc = ForgeRegistries.BLOCKS.getKey(block);
                                    if (bLoc != null) {
                                        String bId = bLoc.toString();
                                        if (isBlockTracked(bId, block, state)) {
                                            if (foundPositions.add(bPos)) {
                                                newTargets.add(new BlockTarget(bPos, block, bId));
                                            }
                                        }
                                    }
                                }

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
                                                        if (bPos.distSqr(pPos) <= maxDist * maxDist) {
                                                            if (foundPositions.add(bPos)) {
                                                                newTargets.add(new BlockTarget(bPos, block, bId));
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

                        synchronized (CACHED_BLOCK_TARGETS) {
                            CACHED_BLOCK_TARGETS.clear();
                            CACHED_BLOCK_TARGETS.addAll(newTargets);
                        }
                    } catch (Exception ignored) {
                    } finally {
                        isScanning = false;
                    }
                });
            }
        }
    }

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

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        // 开启 100% 极限 OpenGL 无视深度遮挡穿透绘制
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        RenderSystem.lineWidth(3.0F);

        builder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // 1. 绘制实体/怪物/掉落物 3D 透视边框与射线
        for (Entity entity : level.entitiesForRendering()) {
            if (entity == player) continue;
            double distSq = entity.distanceToSqr(player);
            if (distSq > TrackerConfig.maxDistance * TrackerConfig.maxDistance) continue;

            String trackerId = null;
            int color = 0xFF00FF00;

            if (entity instanceof ItemEntity itemEntity) {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty()) {
                    ResourceLocation itemLoc = ForgeRegistries.ITEMS.getKey(stack.getItem());
                    if (itemLoc != null && isItemTracked(itemLoc.toString())) {
                        trackerId = itemLoc.toString();
                        color = TrackerConfig.getItemColor(trackerId);
                    }
                }
            } else {
                ResourceLocation entityLoc = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
                if (entityLoc != null && isEntityTracked(entityLoc.toString())) {
                    trackerId = entityLoc.toString();
                    color = TrackerConfig.getEntityColor(trackerId);
                }
            }

            if (trackerId != null) {
                AABB box = entity.getBoundingBox().move(-camPos.x, -camPos.y, -camPos.z);
                addBoxToBuffer(poseStack, builder, box, color);

                if (TrackerConfig.showTracers) {
                    addTracerToBuffer(poseStack, builder, camPos, entity.getBoundingBox().getCenter(), color);
                }
            }
        }

        // 2. 绘制缓存的方块/宝箱/矿石/静止伪装箱 3D 透视边框与射线
        synchronized (CACHED_BLOCK_TARGETS) {
            BlockPos pPos = player.blockPosition();
            double maxDistSq = TrackerConfig.maxDistance * TrackerConfig.maxDistance;

            for (BlockTarget target : CACHED_BLOCK_TARGETS) {
                if (!TrackerConfig.trackedBlocks.containsKey(target.id)) continue;

                BlockPos bPos = target.pos;
                double distSq = bPos.distSqr(pPos);
                if (distSq <= maxDistSq) {
                    int color = TrackerConfig.getBlockColor(target.id);
                    AABB box = new AABB(bPos).move(-camPos.x, -camPos.y, -camPos.z);
                    addBoxToBuffer(poseStack, builder, box, color);

                    if (TrackerConfig.showTracers) {
                        addTracerToBuffer(poseStack, builder, camPos, new Vec3(bPos.getX() + 0.5, bPos.getY() + 0.5, bPos.getZ() + 0.5), color);
                    }
                }
            }
        }

        BufferUploader.drawWithShader(builder.end());

        // 恢复 OpenGL 标准绘制管道
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        RenderSystem.lineWidth(1.0F);

        // 3. 绘制文字悬浮标签 (使用 SEE_THROUGH 无视遮挡模式)
        if (TrackerConfig.showDistanceText) {
            MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

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
                    String text = String.format(Locale.ROOT, "%s (%.1fm)", name, dist);
                    renderBillboardText(poseStack, bufferSource, mc.font, text, entity.getX() - camPos.x, entity.getY() + entity.getBbHeight() + 0.4 - camPos.y, entity.getZ() - camPos.z, color);
                }
            }

            synchronized (CACHED_BLOCK_TARGETS) {
                BlockPos pPos = player.blockPosition();
                double maxDistSq = TrackerConfig.maxDistance * TrackerConfig.maxDistance;

                for (BlockTarget target : CACHED_BLOCK_TARGETS) {
                    if (!TrackerConfig.trackedBlocks.containsKey(target.id)) continue;

                    BlockPos bPos = target.pos;
                    double distSq = bPos.distSqr(pPos);
                    if (distSq <= maxDistSq) {
                        int color = TrackerConfig.getBlockColor(target.id);
                        String name = ChineseNameMapper.getBlockName(target.block, target.id);
                        String text = String.format(Locale.ROOT, "%s (%.1fm)", name, Math.sqrt(distSq));
                        renderBillboardText(poseStack, bufferSource, mc.font, text, bPos.getX() + 0.5 - camPos.x, bPos.getY() + 1.2 - camPos.y, bPos.getZ() + 0.5 - camPos.z, color);
                    }
                }
            }
        }
    }

    private static boolean isEntityTracked(String id) {
        return TrackerConfig.trackedEntities.containsKey(id);
    }

    private static boolean isItemTracked(String id) {
        return TrackerConfig.trackedItems.containsKey(id);
    }

    public static boolean isBlockTracked(String bId, Block block, BlockState state) {
        return TrackerConfig.trackedBlocks.containsKey(bId);
    }

    private static void addBoxToBuffer(PoseStack poseStack, VertexConsumer consumer, AABB box, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = 1.0F;

        Matrix4f mat = poseStack.last().pose();

        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;

        // 12 条立方体边框线
        drawLine(consumer, mat, minX, minY, minZ, maxX, minY, minZ, r, g, b, a);
        drawLine(consumer, mat, maxX, minY, minZ, maxX, minY, maxZ, r, g, b, a);
        drawLine(consumer, mat, maxX, minY, maxZ, minX, minY, maxZ, r, g, b, a);
        drawLine(consumer, mat, minX, minY, maxZ, minX, minY, minZ, r, g, b, a);

        drawLine(consumer, mat, minX, maxY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(consumer, mat, maxX, maxY, minZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, mat, maxX, maxY, maxZ, minX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, mat, minX, maxY, minZ, minX, maxY, minZ, r, g, b, a);

        drawLine(consumer, mat, minX, minY, minZ, minX, maxY, minZ, r, g, b, a);
        drawLine(consumer, mat, maxX, minY, minZ, maxX, maxY, minZ, r, g, b, a);
        drawLine(consumer, mat, maxX, minY, maxZ, maxX, maxY, maxZ, r, g, b, a);
        drawLine(consumer, mat, minX, minY, maxZ, minX, maxY, maxZ, r, g, b, a);
    }

    private static void addTracerToBuffer(PoseStack poseStack, VertexConsumer consumer, Vec3 camPos, Vec3 targetCenter, int color) {
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        float a = 1.0F;

        Matrix4f mat = poseStack.last().pose();
        Vec3 end = targetCenter.subtract(camPos);

        drawLine(consumer, mat, 0, 0, 0, end.x, end.y, end.z, r, g, b, a);
    }

    private static void drawLine(VertexConsumer consumer, Matrix4f mat, double x1, double y1, double z1, double x2, double y2, double z2, float r, float g, float b, float a) {
        consumer.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        consumer.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
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
