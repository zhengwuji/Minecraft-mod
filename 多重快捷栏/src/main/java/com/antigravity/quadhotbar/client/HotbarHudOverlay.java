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

    // 需要向上避让抬高 22 像素的 HUD 元素 ID 关键字
    private static boolean shouldShiftUp(String id) {
        if (id == null) return false;
        String lower = id.toLowerCase();
        return lower.contains("health") || lower.contains("armor") || lower.contains("food") ||
                lower.contains("air") || lower.contains("experience") || lower.contains("mount") ||
                lower.contains("thirst") || lower.contains("item_name") || lower.contains("status");
    }

    /**
     * 在 Pre 阶段给血量、护甲、经验条、口渴水滴等 HUD 添加向上 22 像素的临时平移
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreRenderOverlay(RenderGuiOverlayEvent.Pre event) {
        String overlayId = event.getOverlay().id().toString();

        if (shouldShiftUp(overlayId)) {
            event.getGuiGraphics().pose().pushPose();
            event.getGuiGraphics().pose().translate(0, -22, 0);
        }
    }

    /**
     * 在 Post 阶段恢复平移，弹栈还原
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post event) {
        String overlayId = event.getOverlay().id().toString();

        if (shouldShiftUp(overlayId)) {
            event.getGuiGraphics().pose().popPose();
        }

        // 当原版 Hotbar 渲染完成后，绘制第二层快捷栏
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id())) {
            renderSecondHotbarLayer(event.getGuiGraphics());
        }
    }

    private static void renderSecondHotbarLayer(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 原版快捷栏居中，宽度 182，起点为 screenWidth / 2 - 91
        int left = screenWidth / 2 - 91;
        // 原版快捷栏顶部坐标为 screenHeight - 22。第二层快捷栏绘制在 screenHeight - 44 处
        int top = screenHeight - 44;

        // 1. 完全隔离局部 Matrix 栈
        guiGraphics.pose().pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

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

        // 4. 强制提交局域批处理，清空 Depth 深度测试与 Lighting 状态
        guiGraphics.flush();
        RenderSystem.disableDepthTest();

        // 5. 归还全局 Pose 栈，确保与背包及任何后续界面绝对隔离
        guiGraphics.pose().popPose();
    }
}
