package com.antigravity.foodbuffbag.client;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import com.antigravity.foodbuffbag.network.PacketOpenFoodBag;
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

            // 用户截图标注红圈位置：原版背包顶部第 4 个标签按钮处 (x = guiLeft + 126, y = guiTop - 24)
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
        public CustomFoodButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
            this.setTooltip(net.minecraft.client.gui.components.Tooltip.create(Component.translatable("tooltip.foodbuffbag.open_bag")));
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            // 在按钮中央渲染金苹果图标作为食物仓标识
            ItemStack goldenApple = new ItemStack(Items.GOLDEN_APPLE);
            guiGraphics.renderItem(goldenApple, this.getX() + 4, this.getY() + 4);
        }
    }
}
