package com.antigravity.foodbuffbag.client;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import com.antigravity.foodbuffbag.network.PacketOpenFoodBag;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = FoodBuffBag.MOD_ID, value = Dist.CLIENT)
public class InventoryScreenButtonHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            int guiLeft = screen.getGuiLeft();
            int guiTop = screen.getGuiTop();

            // 原版背包顶部红圈标注位置 (x = guiLeft + 126, y = guiTop - 24)
            int btnX = guiLeft + 126;
            int btnY = guiTop - 24;
            int btnWidth = 24;
            int btnHeight = 24;

            Button customFoodButton = new CustomFoodButton(
                    btnX, btnY, btnWidth, btnHeight,
                    Component.empty(),
                    (btn) -> NetworkHandler.CHANNEL.sendToServer(new PacketOpenFoodBag())
            );

            event.addListener(customFoodButton);
        }
    }

    private static class CustomFoodButton extends Button {
        private static final ItemStack GOLDEN_APPLE = new ItemStack(Items.GOLDEN_APPLE);

        public CustomFoodButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("tooltip.foodbuffbag.open_bag")));
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            // 1. 隔离外层 Pose 矩阵
            guiGraphics.pose().pushPose();

            // 2. 渲染按钮标准背景
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

            // 3. 在完全独立的局域 PoseStack 内部渲染金苹果图标
            guiGraphics.pose().pushPose();
            guiGraphics.renderItem(GOLDEN_APPLE, this.getX() + 4, this.getY() + 4);
            guiGraphics.pose().popPose();

            // 4. 强制刷新提交批处理，防止状态泄漏
            guiGraphics.flush();

            // 5. 还原平面 2D 光照及禁用 3D 深度测试，彻底防止污染 InventoryScreen 后续槽位高亮与 Tooltip 绘制
            Lighting.setupForFlatItems();
            RenderSystem.disableDepthTest();

            // 6. 归还全局 2D 矩阵
            guiGraphics.pose().popPose();
        }
    }
}
