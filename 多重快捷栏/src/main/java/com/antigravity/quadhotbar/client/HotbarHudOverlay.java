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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuadHotbar.MOD_ID, value = Dist.CLIENT)
public class HotbarHudOverlay {

    @SuppressWarnings("removal")
    private static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("textures/gui/widgets.png");

    @SubscribeEvent
    public static void onRenderHotbarOverlay(RenderGuiOverlayEvent.Post event) {
        // 只在原版 Hotbar 渲染完成后叠加绘制第二层快捷栏
        if (!event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 原版快捷栏居中，宽度 182，起点为 screenWidth / 2 - 91
        int left = screenWidth / 2 - 91;
        // 原版快捷栏顶部坐标是 screenHeight - 22。第二层快捷栏放置在 screenHeight - 42 处
        int top = screenHeight - 42;

        // 1. 完全隔离局部 Matrix 栈，绝对不泄露平移状态
        guiGraphics.pose().pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 2. 绘制第二层快捷栏背景 (9 个槽位)
        // 绘制通用 182x22 的 Hotbar 背景纹理
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

        // 4. 强制提交局域批处理，清空 Depth 深度测试与 Lighting 状态
        guiGraphics.flush();
        RenderSystem.disableDepthTest();

        // 5. 归还全局 Pose 栈，确保与背包及任何后续界面绝对隔离
        guiGraphics.pose().popPose();
    }
}
