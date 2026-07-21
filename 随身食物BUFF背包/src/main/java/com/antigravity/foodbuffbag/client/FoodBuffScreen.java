package com.antigravity.foodbuffbag.client;

import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import com.antigravity.foodbuffbag.network.PacketChangePage;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

public class FoodBuffScreen extends AbstractContainerScreen<FoodBuffMenu> {
    @SuppressWarnings("removal")
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");

    private Button prevPageBtn;
    private Button nextPageBtn;

    public FoodBuffScreen(FoodBuffMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 在 GUI 顶部标题右侧放置翻页按钮 [ < ] 和 [ > ]
        this.prevPageBtn = Button.builder(Component.literal("<"), (btn) -> {
            if (this.menu.getCurrentPage() > 0) {
                NetworkHandler.CHANNEL.sendToServer(new PacketChangePage(-1));
                this.menu.changePage(-1);
            }
        })
        .bounds(x + 120, y + 4, 16, 12)
        .build();

        this.nextPageBtn = Button.builder(Component.literal(">"), (btn) -> {
            if (this.menu.getCurrentPage() < FoodBuffProvider.MAX_PAGES - 1) {
                NetworkHandler.CHANNEL.sendToServer(new PacketChangePage(1));
                this.menu.changePage(1);
            }
        })
        .bounds(x + 154, y + 4, 16, 12)
        .build();

        this.addRenderableWidget(prevPageBtn);
        this.addRenderableWidget(nextPageBtn);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 在 [ < ] 与 [ > ] 按钮中间绘制页码，例如 "1 / 100"
        String pageText = (this.menu.getCurrentPage() + 1) + "/" + FoodBuffProvider.MAX_PAGES;
        guiGraphics.drawString(this.font, pageText, x + 138 - this.font.width(pageText) / 2, y + 6, 0x404040, false);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
