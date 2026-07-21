package com.tfar.anviltweaks;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ItemCombinerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

@SuppressWarnings("removal")
public class AnvilScreenv2 extends ItemCombinerScreen<RepairContainerv2> {

    private static final ResourceLocation ANVIL_LOCATION = new ResourceLocation("minecraft", "textures/gui/container/anvil.png");

    public AnvilScreenv2(RepairContainerv2 menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, ANVIL_LOCATION);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderErrorIcon(GuiGraphics guiGraphics, int x, int y) {
        if ((this.menu.getSlot(0).hasItem() || this.menu.getSlot(1).hasItem()) && !this.menu.getSlot(this.menu.getResultSlot()).hasItem()) {
            guiGraphics.blit(ANVIL_LOCATION, x + 99, y + 45, this.imageWidth, 0, 28, 21);
        }
    }
}
