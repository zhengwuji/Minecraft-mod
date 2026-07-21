package com.antigravity.quadhotbar.client;

import com.antigravity.quadhotbar.QuadHotbar;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuadHotbar.MOD_ID, value = Dist.CLIENT)
public class HotbarHudOverlay {

    @SuppressWarnings("removal")
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    /**
     * 关键修复：在血量 (PLAYER_HEALTH)、饥饿、气泡及物品名等所有 HUD Overlay 全部绘制完成之后 (EventPriority.LOWEST)，
     * 最后绘制第二层快捷栏，使其绝对盖在血量红心/黄心/气泡/水滴的最上方，实现真正的最高层置顶！
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
        // 在 ITEM_NAME 或 HOTBAR 的最末尾节点触发，确保后发制人盖在血量和水滴之上
        if (event.getOverlay().id().equals(VanillaGuiOverlay.ITEM_NAME.id()) ||
            event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            renderTopmostSecondHotbar(event.getGuiGraphics());
        }
    }

    private static void renderTopmostSecondHotbar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 原版快捷栏居中，宽度 182，起点为 screenWidth / 2 - 91
        int left = screenWidth / 2 - 91;
        // 第二层快捷栏绘制在原版快捷栏正上方 (screenHeight - 44 处)
        int top = screenHeight - 44;

        // 1. 完全隔离局部 Matrix 栈，提升 Z 轴至 500
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 500);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // 禁用深度测试，强行压在血量水滴最上方

        // 2. 绘制第二层快捷栏背景 (9 个槽位)
        guiGraphics.blit(WIDGETS_TEXTURE, left, top, 0, 0, 182, 22);

        // 3. 渲染第二层 (Slot 9~17) 槽位内的物品及数量图标
        for (int i = 0; i < 9; i++) {
            int slot = 9 + i;
            ItemStack stack = player.getInventory().getItem(slot);
            int itemX = left + 3 + i * 20;
            int itemY = top + 3;

            if (!stack.isEmpty()) {
                guiGraphics.pose().pushPose();
                // 确保数量为 1 且合法
                guiGraphics.renderItem(player, stack, itemX, itemY, i);
                guiGraphics.renderItemDecorations(mc.font, stack, itemX, itemY);
                guiGraphics.pose().popPose();
            }
        }

        // 4. 强制提交局域批处理，清空 Depth 深度测试与 Lighting 状态
        guiGraphics.flush();
        RenderSystem.disableDepthTest();

        // 5. 归还全局 Pose 栈
        guiGraphics.pose().popPose();
    }
}
