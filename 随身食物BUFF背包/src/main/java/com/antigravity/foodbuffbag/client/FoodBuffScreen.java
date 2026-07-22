package com.antigravity.foodbuffbag.client;

import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import com.antigravity.foodbuffbag.network.PacketChangePage;
import com.antigravity.foodbuffbag.network.PacketQuickDeposit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FoodBuffScreen extends AbstractContainerScreen<FoodBuffMenu> {
    @SuppressWarnings("removal")
    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");

    private Button prevPageBtn;
    private Button nextPageBtn;
    private Button quickDepositBtn;

    public FoodBuffScreen(FoodBuffMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        // 1. 翻页按钮 [ < ] (x + 65)
        this.prevPageBtn = Button.builder(Component.literal("<"), (btn) -> {
            if (this.menu.getCurrentPage() > 0) {
                NetworkHandler.CHANNEL.sendToServer(new PacketChangePage(-1));
                this.menu.changePage(-1);
            }
        })
        .bounds(x + 65, y + 3, 14, 12)
        .build();

        // 2. 翻页按钮 [ > ] (x + 115)
        this.nextPageBtn = Button.builder(Component.literal(">"), (btn) -> {
            if (this.menu.getCurrentPage() < FoodBuffProvider.MAX_PAGES - 1) {
                NetworkHandler.CHANNEL.sendToServer(new PacketChangePage(1));
                this.menu.changePage(1);
            }
        })
        .bounds(x + 115, y + 3, 14, 12)
        .build();

        // 3. [存入] 快速归仓按钮 (x + 132, 宽度 36)
        this.quickDepositBtn = Button.builder(Component.translatable("tooltip.foodbuffbag.quick_deposit"), (btn) -> {
            NetworkHandler.CHANNEL.sendToServer(new PacketQuickDeposit());
        })
        .bounds(x + 132, y + 3, 36, 12)
        .tooltip(Tooltip.create(Component.translatable("tooltip.foodbuffbag.quick_deposit_desc")))
        .build();

        this.addRenderableWidget(prevPageBtn);
        this.addRenderableWidget(nextPageBtn);
        this.addRenderableWidget(quickDepositBtn);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // 渲染当前页码，如 "1/100" (精确居中于 < 与 > 按钮之间 x+97 位置)
        String pageText = (this.menu.getCurrentPage() + 1) + "/" + FoodBuffProvider.MAX_PAGES;
        guiGraphics.drawString(this.font, pageText, x + 97 - this.font.width(pageText) / 2, y + 5, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // 在标题右侧精准绘制 [🌟] BUFF 提示图标 (x + 48)，绝不与标题或按钮打架
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        int starX = x + 48;
        int starY = y + 5;

        guiGraphics.drawString(this.font, "🌟", starX, starY, 0xFFD700, false);

        // 鼠标悬停在 [🌟] 图标区域时，弹框展示玩家当前激活的所有 BUFF 清单总览
        if (mouseX >= starX && mouseX <= starX + 12 && mouseY >= starY && mouseY <= starY + 10) {
            List<Component> tooltipList = new ArrayList<>();
            tooltipList.add(Component.translatable("tooltip.foodbuffbag.buff_overview"));

            if (Minecraft.getInstance().player != null) {
                Collection<MobEffectInstance> effects = Minecraft.getInstance().player.getActiveEffects();
                if (effects.isEmpty()) {
                    tooltipList.add(Component.translatable("tooltip.foodbuffbag.no_active_buffs"));
                } else {
                    for (MobEffectInstance effect : effects) {
                        Component effectName = effect.getEffect().getDisplayName();
                        int amp = effect.getAmplifier();
                        String levelStr = amp > 0 ? " " + (amp + 1) : "";
                        tooltipList.add(Component.literal("  §a✔ §f").append(effectName).append(levelStr).append(" §7(永久)"));
                    }
                }
            }
            guiGraphics.renderComponentTooltip(this.font, tooltipList, mouseX, mouseY);
        }

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
