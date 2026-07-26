package com.customemc.client.gui;

import com.customemc.ConfigManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class CustomEMCScreen extends Screen {

    private EditBox defaultEmcBox;
    private EditBox itemIdBox;
    private EditBox customPriceBox;

    private String statusMessage = "";
    private int statusColor = 0xFF55FF55;

    public CustomEMCScreen() {
        super(Component.literal("自定义等价交换EMC 设置面板"));
    }

    @Override
    protected void init() {
        super.init();

        int panelWidth = 320;
        int panelHeight = 210;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        ConfigManager.loadConfig();

        this.defaultEmcBox = new EditBox(this.font, left + 155, top + 42, 140, 18, Component.literal("默认EMC"));
        this.defaultEmcBox.setValue(String.valueOf(ConfigManager.getDefaultEMC()));
        this.addRenderableWidget(this.defaultEmcBox);

        this.itemIdBox = new EditBox(this.font, left + 20, top + 95, 170, 18, Component.literal("物品ID"));
        this.itemIdBox.setValue("minecraft:dirt");
        this.addRenderableWidget(this.itemIdBox);

        this.addRenderableWidget(Button.builder(Component.literal("抓取主手物品"), btn -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                ItemStack mainHand = this.minecraft.player.getMainHandItem();
                if (!mainHand.isEmpty()) {
                    ResourceLocation loc = ForgeRegistries.ITEMS.getKey(mainHand.getItem());
                    if (loc != null) {
                        this.itemIdBox.setValue(loc.toString());
                        this.statusMessage = "已抓取主手物品: " + loc;
                        this.statusColor = 0xFF55FF55;
                    }
                } else {
                    this.statusMessage = "主手没有按持有任何物品！";
                    this.statusColor = 0xFFFF5555;
                }
            }
        }).bounds(left + 200, top + 94, 100, 20).build());

        this.customPriceBox = new EditBox(this.font, left + 20, top + 135, 170, 18, Component.literal("特定价格"));
        this.customPriceBox.setValue("10000");
        this.addRenderableWidget(this.customPriceBox);

        this.addRenderableWidget(Button.builder(Component.literal("添加/更新指定价格"), btn -> {
            String id = this.itemIdBox.getValue().trim();
            String priceStr = this.customPriceBox.getValue().trim();
            if (id.isEmpty()) {
                this.statusMessage = "请输入有效的物品ID！";
                this.statusColor = 0xFFFF5555;
                return;
            }
            try {
                long price = Long.parseLong(priceStr);
                ConfigManager.setCustomPrice(id, price);
                this.statusMessage = "成功添加/更新: " + id + " = " + price;
                this.statusColor = 0xFF55FF55;
            } catch (NumberFormatException e) {
                this.statusMessage = "价格格式无效，请输入数字！";
                this.statusColor = 0xFFFF5555;
            }
        }).bounds(left + 200, top + 134, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("保存默认EMC"), btn -> {
            String valStr = this.defaultEmcBox.getValue().trim();
            try {
                long val = Long.parseLong(valStr);
                ConfigManager.setDefaultEMC(val);
                this.statusMessage = "默认EMC已更新为: " + val;
                this.statusColor = 0xFF55FF55;
            } catch (NumberFormatException e) {
                this.statusMessage = "默认EMC格式无效！";
                this.statusColor = 0xFFFF5555;
            }
        }).bounds(left + 20, top + 175, 130, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("重载并更新 ProjectE EMC"), btn -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.connection.sendUnsignedCommand("reload");
                com.customemc.AutoKnowledgeHandler.grantFullKnowledge(this.minecraft.player);
                this.statusMessage = "已成功发送 EMC 重载指令 (/reload) 并刷新全知识状态！";
                this.statusColor = 0xFF55FF55;
            }
        }).bounds(left + 160, top + 175, 140, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);

        int panelWidth = 320;
        int panelHeight = 210;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;

        guiGraphics.fill(left, top, left + panelWidth, top + panelHeight, 0xEE1A1A1A);
        guiGraphics.renderOutline(left, top, panelWidth, panelHeight, 0xFF444444);

        guiGraphics.drawCenteredString(this.font, this.title, left + panelWidth / 2, top + 12, 0xFFFFFF00);

        guiGraphics.drawString(this.font, "未指定EMC的物品默认价格:", left + 20, top + 47, 0xFFDDDDDD, false);
        guiGraphics.drawString(this.font, "物品 ID (如 minecraft:diamond):", left + 20, top + 82, 0xFFAAAAAA, false);
        guiGraphics.drawString(this.font, "特定 EMC 价格:", left + 20, top + 122, 0xFFAAAAAA, false);

        if (!this.statusMessage.isEmpty()) {
            guiGraphics.drawCenteredString(this.font, this.statusMessage, left + panelWidth / 2, top + 160, this.statusColor);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
