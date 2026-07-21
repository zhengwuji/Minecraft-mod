package com.antigravity.foodbuffbag.client;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import com.antigravity.foodbuffbag.network.PacketOpenFoodBag;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 完全绕开原版 Widget 系统渲染食物仓按钮：
 *   - 不使用 event.addListener()，避免触发 InventoryScreen 的 widget 边界重算导致槽位 hover 偏移
 *   - 通过 ScreenEvent.Render.Post 手动绘制按钮
 *   - 通过 ScreenEvent.MouseButtonPressed.Pre 手动处理点击
 */
@Mod.EventBusSubscriber(modid = FoodBuffBag.MOD_ID, value = Dist.CLIENT)
public class InventoryScreenButtonHandler {

    private static final ItemStack GOLDEN_APPLE = new ItemStack(Items.GOLDEN_APPLE);
    private static final int BTN_W = 20;
    private static final int BTN_H = 20;

    /** 计算按钮左上角坐标 */
    private static int[] getBtnPos(InventoryScreen screen) {
        int x = screen.getGuiLeft() + 126;
        int y = screen.getGuiTop() - 22;
        return new int[]{x, y};
    }

    /** 在 GUI 绘制完成后，手动在顶部绘制金苹果按钮（完全独立于 Widget 系统） */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;

        int[] pos = getBtnPos(screen);
        int bx = pos[0], by = pos[1];
        int mx = (int) event.getMouseX(), my = (int) event.getMouseY();
        boolean hovered = mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H;

        GuiGraphics gfx = event.getGuiGraphics();

        // 1. 绘制按钮背景（使用原版 button 纹理：y=46 正常态, y=66 悬停态）
        RenderSystem.enableBlend();
        gfx.setColor(1f, 1f, 1f, 1f);
        // 简洁半透明背景：悬停时亮，普通时暗
        int bgColor = hovered ? 0xAAFFFFFF : 0x66AAAAAA;
        gfx.fill(bx, by, bx + BTN_W, by + BTN_H, bgColor);
        // 边框
        gfx.fill(bx, by, bx + BTN_W, by + 1, 0xFF555555);
        gfx.fill(bx, by + BTN_H - 1, bx + BTN_W, by + BTN_H, 0xFF555555);
        gfx.fill(bx, by, bx + 1, by + BTN_H, 0xFF555555);
        gfx.fill(bx + BTN_W - 1, by, bx + BTN_W, by + BTN_H, 0xFF555555);

        // 2. 绘制金苹果图标（居中）
        gfx.pose().pushPose();
        gfx.renderItem(GOLDEN_APPLE, bx + 2, by + 2);
        gfx.pose().popPose();
        // flush 立即提交，不污染后续 UI 状态
        gfx.flush();
        RenderSystem.disableDepthTest();

        // 3. 悬停时显示 Tooltip
        if (hovered) {
            gfx.renderTooltip(
                Minecraft.getInstance().font,
                Component.translatable("tooltip.foodbuffbag.open_bag"),
                mx, my
            );
        }
    }

    /** 手动处理鼠标点击，匹配按钮区域即发送打开背包数据包 */
    @SubscribeEvent
    public static void onMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        if (event.getButton() != 0) return; // 仅左键

        int[] pos = getBtnPos(screen);
        int bx = pos[0], by = pos[1];
        double mx = event.getMouseX(), my = event.getMouseY();

        if (mx >= bx && mx < bx + BTN_W && my >= by && my < by + BTN_H) {
            NetworkHandler.CHANNEL.sendToServer(new PacketOpenFoodBag());
            event.setCanceled(true); // 拦截点击，防止透传给原版
        }
    }
}
