package com.antigravity.quadhotbar.client;

import com.antigravity.quadhotbar.QuadHotbar;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuadHotbar.MOD_ID, value = Dist.CLIENT)
public class HotbarHudOverlay {

    @SuppressWarnings("removal")
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    /**
     * 关键置顶逻辑：在 RenderGuiEvent.Post (整套 2D GUI 包含所有第三方心形/护甲/经验条 HUD 绘制完成后)
     * 最后一刻绘制第二层快捷栏！配合 Z=999 与 disableDepthTest，实现 100% 绝对置顶盖在所有元素最上方！
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.options.hideGui) return; // 只有在游戏主画面且没有打开窗口时置顶显示

        renderAbsoluteTopmostSecondHotbar(event.getGuiGraphics());
    }

    private static void renderAbsoluteTopmostSecondHotbar(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 原版快捷栏居中，宽度 182，起点为 screenWidth / 2 - 91
        int left = screenWidth / 2 - 91;
        // 第二层快捷栏绘制在原版快捷栏正上方 (screenHeight - 44 处)
        int top = screenHeight - 44;

        // 1. 建立独占 Pose 矩阵，提升 Z 轴至 999 绝对顶层
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 999);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // 禁用深度测试，绝对压盖最上层

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
                guiGraphics.renderItem(player, stack, itemX, itemY, i);
                guiGraphics.renderItemDecorations(mc.font, stack, itemX, itemY);
                guiGraphics.pose().popPose();
            }
        }

        // 4. 强制刷新渲染管线
        guiGraphics.flush();
        RenderSystem.disableDepthTest();

        // 5. 归还 Pose 矩阵，零内存/状态污染
        guiGraphics.pose().popPose();
    }
}
