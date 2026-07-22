package com.antigravity.devhelper.client.gui;

import com.antigravity.devhelper.network.C2SUpdateAttributePacket;
import com.antigravity.devhelper.network.C2SUpdatePlayerStatsPacket;
import com.antigravity.devhelper.network.DevHelperNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DevHelperScreen extends Screen {
    private EditBox searchBox;
    private EditBox valueInputBox;
    private EditBox playerStatInputBox;

    private final List<AttributeItem> allAttributes = new ArrayList<>();
    private final List<AttributeItem> filteredAttributes = new ArrayList<>();
    private AttributeItem selectedAttribute = null;

    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 24;
    private static final int VISIBLE_ITEMS = 6;

    private int activeTab = 0; // 0: 属性修改, 1: 玩家状态
    private String statusMessage = "";
    private long statusTimer = 0;

    public static class AttributeItem {
        public final String id;
        public final Attribute attribute;
        public final double currentValue;
        public final double baseValue;

        public AttributeItem(String id, Attribute attribute, double currentValue, double baseValue) {
            this.id = id;
            this.attribute = attribute;
            this.currentValue = currentValue;
            this.baseValue = baseValue;
        }
    }

    public DevHelperScreen() {
        super(Component.translatable("gui.devhelper.title"));
    }

    @Override
    protected void init() {
        super.init();
        refreshAttributeList();

        int centerX = this.width / 2;
        int guiLeft = centerX - 190;
        int guiTop = 20;

        // 页签切换按钮
        this.addRenderableWidget(Button.builder(Component.literal("全模组属性修改"), btn -> {
            this.activeTab = 0;
            rebuildWidgets();
        }).bounds(guiLeft, guiTop + 5, 120, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("玩家基础快捷状态"), btn -> {
            this.activeTab = 1;
            rebuildWidgets();
        }).bounds(guiLeft + 125, guiTop + 5, 120, 20).build());

        if (this.activeTab == 0) {
            // 搜索框
            this.searchBox = new EditBox(this.font, guiLeft + 10, guiTop + 32, 360, 18, Component.literal("Search"));
            this.searchBox.setHint(Component.translatable("gui.devhelper.search"));
            this.searchBox.setResponder(text -> filterList());
            this.addRenderableWidget(this.searchBox);

            // 修改数值输入框与按钮
            this.valueInputBox = new EditBox(this.font, guiLeft + 10, guiTop + 205, 100, 18, Component.literal("Value"));
            this.addRenderableWidget(this.valueInputBox);

            this.addRenderableWidget(Button.builder(Component.literal("应用设定"), btn -> applyValue())
                    .bounds(guiLeft + 115, guiTop + 204, 65, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+10"), btn -> addValue(10))
                    .bounds(guiLeft + 185, guiTop + 204, 40, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+100"), btn -> addValue(100))
                    .bounds(guiLeft + 230, guiTop + 204, 45, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("1000"), btn -> setValueDirectly(1000))
                    .bounds(guiLeft + 280, guiTop + 204, 45, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("重置默认"), btn -> resetDefaultValue())
                    .bounds(guiLeft + 330, guiTop + 204, 50, 20).build());
        } else {
            // 玩家基础状态页签按钮
            this.addRenderableWidget(Button.builder(Component.literal("一键回复满血满饱食度"), btn -> {
                DevHelperNetwork.CHANNEL.sendToServer(new C2SUpdatePlayerStatsPacket(C2SUpdatePlayerStatsPacket.Action.HEAL_FULL, 0));
                showStatus("已成功回复玩家状态！");
            }).bounds(guiLeft + 20, guiTop + 50, 160, 22).build());

            this.playerStatInputBox = new EditBox(this.font, guiLeft + 20, guiTop + 90, 100, 18, Component.literal("StatValue"));
            this.playerStatInputBox.setValue("100");
            this.addRenderableWidget(this.playerStatInputBox);

            this.addRenderableWidget(Button.builder(Component.literal("设置当前血量"), btn -> {
                try {
                    float val = Float.parseFloat(this.playerStatInputBox.getValue());
                    DevHelperNetwork.CHANNEL.sendToServer(new C2SUpdatePlayerStatsPacket(C2SUpdatePlayerStatsPacket.Action.SET_HEALTH, val));
                    showStatus("血量已提交更改！");
                } catch (Exception ignored) {}
            }).bounds(guiLeft + 130, guiTop + 89, 100, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("设置经验等级"), btn -> {
                try {
                    float val = Float.parseFloat(this.playerStatInputBox.getValue());
                    DevHelperNetwork.CHANNEL.sendToServer(new C2SUpdatePlayerStatsPacket(C2SUpdatePlayerStatsPacket.Action.SET_XP_LEVEL, val));
                    showStatus("经验等级已更改！");
                } catch (Exception ignored) {}
            }).bounds(guiLeft + 240, guiTop + 89, 100, 20).build());
        }
    }

    private void refreshAttributeList() {
        allAttributes.clear();
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        for (ResourceLocation loc : ForgeRegistries.ATTRIBUTES.getKeys()) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(loc);
            if (attribute != null) {
                AttributeInstance inst = player.getAttribute(attribute);
                if (inst != null) {
                    allAttributes.add(new AttributeItem(loc.toString(), attribute, inst.getValue(), inst.getBaseValue()));
                }
            }
        }
        filterList();
    }

    private void filterList() {
        filteredAttributes.clear();
        String query = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";
        for (AttributeItem item : allAttributes) {
            if (query.isEmpty() || item.id.toLowerCase(Locale.ROOT).contains(query)) {
                filteredAttributes.add(item);
            }
        }
        this.scrollOffset = 0;
    }

    private void applyValue() {
        if (selectedAttribute == null || valueInputBox == null) return;
        try {
            double val = Double.parseDouble(valueInputBox.getValue());
            DevHelperNetwork.CHANNEL.sendToServer(new C2SUpdateAttributePacket(selectedAttribute.id, val));
            showStatus("已成功向服务端提交 " + selectedAttribute.id + " -> " + val);
            refreshAttributeList();
        } catch (NumberFormatException e) {
            showStatus("请输入有效的数值！");
        }
    }

    private void addValue(double add) {
        if (selectedAttribute == null) return;
        double next = selectedAttribute.baseValue + add;
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.2f", next));
        applyValue();
    }

    private void setValueDirectly(double val) {
        if (selectedAttribute == null) return;
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.2f", val));
        applyValue();
    }

    private void resetDefaultValue() {
        if (selectedAttribute == null) return;
        double defaultVal = selectedAttribute.attribute.getDefaultValue();
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.2f", defaultVal));
        applyValue();
    }

    private void showStatus(String msg) {
        this.statusMessage = msg;
        this.statusTimer = System.currentTimeMillis() + 3000;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.activeTab == 0 && !filteredAttributes.isEmpty()) {
            if (delta < 0 && scrollOffset < filteredAttributes.size() - VISIBLE_ITEMS) {
                scrollOffset++;
                return true;
            } else if (delta > 0 && scrollOffset > 0) {
                scrollOffset--;
                return true;
            }
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;
        int guiLeft = centerX - 190;
        int guiTop = 20;

        if (this.activeTab == 0 && button == 0) {
            int listTop = guiTop + 55;
            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredAttributes.size()) break;
                int itemY = listTop + i * ITEM_HEIGHT;
                if (mouseX >= guiLeft + 10 && mouseX <= guiLeft + 370 && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT - 2) {
                    this.selectedAttribute = filteredAttributes.get(index);
                    if (this.valueInputBox != null) {
                        this.valueInputBox.setValue(String.format(Locale.ROOT, "%.2f", selectedAttribute.baseValue));
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int guiLeft = centerX - 190;
        int guiTop = 20;

        // 背景深色主面板
        graphics.fill(guiLeft, guiTop, guiLeft + 380, guiTop + 235, 0xEE1A1A24);
        graphics.renderOutline(guiLeft, guiTop, 380, 235, 0xFF3D3D56);

        // 标题
        graphics.drawString(this.font, this.title, guiLeft + 10, guiTop + 10, 0xFFFFFF);

        if (this.activeTab == 0) {
            // 绘制属性列表区域背景
            int listTop = guiTop + 55;
            int listHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            graphics.fill(guiLeft + 8, listTop - 2, guiLeft + 372, listTop + listHeight + 2, 0xFF101018);
            graphics.renderOutline(guiLeft + 8, listTop - 2, 364, listHeight + 4, 0xFF2E2E40);

            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredAttributes.size()) break;

                AttributeItem item = filteredAttributes.get(index);
                int itemY = listTop + i * ITEM_HEIGHT;
                boolean isSelected = (selectedAttribute != null && selectedAttribute.id.equals(item.id));

                int bgColor = isSelected ? 0xFF2A3F66 : (i % 2 == 0 ? 0xFF161622 : 0xFF1A1A2A);
                graphics.fill(guiLeft + 10, itemY, guiLeft + 370, itemY + ITEM_HEIGHT - 2, bgColor);

                // 显示 ID 与数值
                String displayId = item.id;
                if (displayId.length() > 32) displayId = displayId.substring(0, 30) + "..";

                graphics.drawString(this.font, displayId, guiLeft + 15, itemY + 6, isSelected ? 0xFFFF55 : 0xDDDDDD);
                String valStr = String.format(Locale.ROOT, "生效:%.1f | 基址:%.1f", item.currentValue, item.baseValue);
                graphics.drawString(this.font, valStr, guiLeft + 225, itemY + 6, 0x55FF55);
            }

            // 滚动提示
            if (filteredAttributes.size() > VISIBLE_ITEMS) {
                String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + VISIBLE_ITEMS, filteredAttributes.size()) + " / " + filteredAttributes.size();
                graphics.drawString(this.font, scrollInfo, guiLeft + 300, guiTop + 36, 0x888888);
            }
        }

        // 显示状态通知消息
        if (System.currentTimeMillis() < this.statusTimer) {
            graphics.drawString(this.font, this.statusMessage, guiLeft + 15, guiTop + 220, 0x55FF55);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
