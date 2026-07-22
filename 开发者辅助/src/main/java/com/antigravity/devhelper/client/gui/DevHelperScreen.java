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

import java.util.*;

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

    // 内置常见与模组属性中文对照字典（包含 PMMO 全技能）
    private static final Map<String, String> CHINESE_MAP = Map.ofEntries(
            Map.entry("minecraft:generic.max_health", "❤️ 最大生命值"),
            Map.entry("minecraft:generic.knockback_resistance", "🛡️ 击退抗性"),
            Map.entry("minecraft:generic.movement_speed", "⚡ 移动速度"),
            Map.entry("minecraft:generic.flying_speed", "🕊️ 飞行速度"),
            Map.entry("minecraft:generic.attack_damage", "⚔️ 近战伤害"),
            Map.entry("minecraft:generic.attack_knockback", "🔨 击退强度"),
            Map.entry("minecraft:generic.attack_speed", "🗡️ 攻击速度"),
            Map.entry("minecraft:generic.armor", "🛡️ 护甲值"),
            Map.entry("minecraft:generic.armor_toughness", "🥷 盔甲韧性"),
            Map.entry("minecraft:generic.luck", "🍀 幸运值"),
            Map.entry("forge:block_reach", "📏 方块挖掘范围"),
            Map.entry("forge:entity_reach", "🗡️ 实体攻击距离"),
            Map.entry("forge:swim_speed", "🏊 游泳速度"),
            Map.entry("forge:nametag_distance", "🏷️ 名牌距离"),
            Map.entry("forge:step_height_addition", "🪜 自动台阶高度"),

            // PMMO 模组技能列表映射
            Map.entry("pmmo:fishing", "🎣 钓鱼技能 [PMMO]"),
            Map.entry("pmmo:combat", "⚔️ 战斗技能 [PMMO]"),
            Map.entry("pmmo:slayer", "🥩 屠夫技能 [PMMO]"),
            Map.entry("pmmo:hunter", "🏹 狩猎技能 [PMMO]"),
            Map.entry("pmmo:sailing", "⛵ 航海技能 [PMMO]"),
            Map.entry("pmmo:mining", "⛏️ 采掘技能 [PMMO]"),
            Map.entry("pmmo:building", "🧱 建造技能 [PMMO]"),
            Map.entry("pmmo:guns", "🔫 枪械技能 [PMMO]"),
            Map.entry("pmmo:grace", "✨ 祈愿技能 [PMMO]"),
            Map.entry("pmmo:swimming", "🏊 游泳技能 [PMMO]"),
            Map.entry("pmmo:agility", "🏃 灵敏技能 [PMMO]"),
            Map.entry("pmmo:woodcutting", "🪓 砍伐技能 [PMMO]"),
            Map.entry("pmmo:excavation", "⛏️ 挖掘技能 [PMMO]"),
            Map.entry("pmmo:magic", "🔮 魔法技能 [PMMO]"),
            Map.entry("pmmo:endurance", "🛡️ 忍耐技能 [PMMO]"),
            Map.entry("pmmo:crafting", "🔨 合成技能 [PMMO]"),
            Map.entry("pmmo:cooking", "🍳 烹饪技能 [PMMO]"),
            Map.entry("pmmo:farming", "🌾 种植技能 [PMMO]"),
            Map.entry("pmmo:smithing", "⚒️ 巧匠技能 [PMMO]"),
            Map.entry("pmmo:alchemy", "🧪 炼金技能 [PMMO]"),
            Map.entry("pmmo:archery", "🏹 箭术技能 [PMMO]"),
            Map.entry("pmmo:flying", "🪽 飞行技能 [PMMO]")
    );

    private static final String[] PMMO_SKILL_IDS = {
            "pmmo:fishing", "pmmo:combat", "pmmo:slayer", "pmmo:hunter", "pmmo:sailing",
            "pmmo:mining", "pmmo:building", "pmmo:guns", "pmmo:grace", "pmmo:swimming",
            "pmmo:agility", "pmmo:woodcutting", "pmmo:excavation", "pmmo:magic", "pmmo:endurance",
            "pmmo:crafting", "pmmo:cooking", "pmmo:farming", "pmmo:smithing", "pmmo:alchemy",
            "pmmo:archery", "pmmo:flying"
    };

    public static class AttributeItem {
        public final String id;
        public final String chineseName;
        public final Attribute attribute;
        public final double currentValue;
        public final double baseValue;

        public AttributeItem(String id, String chineseName, Attribute attribute, double currentValue, double baseValue) {
            this.id = id;
            this.chineseName = chineseName;
            this.attribute = attribute;
            this.currentValue = currentValue;
            this.baseValue = baseValue;
        }
    }

    public DevHelperScreen() {
        super(Component.literal("🛠️ 开发者辅助"));
    }

    @Override
    protected void init() {
        super.init();
        refreshAttributeList();

        int centerX = this.width / 2;
        int guiLeft = centerX - 195;
        int guiTop = 15;

        // 页签切换按钮
        this.addRenderableWidget(Button.builder(Component.literal("全模组属性与技能"), btn -> {
            this.activeTab = 0;
            rebuildWidgets();
        }).bounds(guiLeft + 165, guiTop + 5, 110, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("玩家状态"), btn -> {
            this.activeTab = 1;
            rebuildWidgets();
        }).bounds(guiLeft + 280, guiTop + 5, 95, 20).build());

        if (this.activeTab == 0) {
            // 搜索框：设置醒目无极限字符长度
            this.searchBox = new EditBox(this.font, guiLeft + 10, guiTop + 32, 370, 18, Component.literal("Search"));
            this.searchBox.setMaxLength(100);
            this.searchBox.setHint(Component.literal("🔍 输入任意名称或数字 (如 钓鱼, 1000, 生命, 护甲, luck)..."));
            this.searchBox.setTextColor(0xFFFFFF);
            this.searchBox.setResponder(text -> filterList());
            this.addRenderableWidget(this.searchBox);

            // 修改数值输入框：扩展宽度，支持输入任意超大数字（如 10000, 999999 等）
            this.valueInputBox = new EditBox(this.font, guiLeft + 10, guiTop + 208, 110, 18, Component.literal("Value"));
            this.valueInputBox.setMaxLength(20);
            this.valueInputBox.setTextColor(0xFFFFFF);
            this.addRenderableWidget(this.valueInputBox);

            // 升级版快捷修改按钮行：全面支持超高等级
            this.addRenderableWidget(Button.builder(Component.literal("应用设定"), btn -> applyValue())
                    .bounds(guiLeft + 125, guiTop + 207, 60, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("+1000"), btn -> addValue(1000))
                    .bounds(guiLeft + 190, guiTop + 207, 45, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("1万级"), btn -> setValueDirectly(10000))
                    .bounds(guiLeft + 240, guiTop + 207, 45, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("10万级"), btn -> setValueDirectly(100000))
                    .bounds(guiLeft + 290, guiTop + 207, 50, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("重置"), btn -> resetDefaultValue())
                    .bounds(guiLeft + 345, guiTop + 207, 35, 20).build());
        } else {
            // 玩家基础状态页签按钮
            this.addRenderableWidget(Button.builder(Component.literal("一键回复满血满饱食度"), btn -> {
                DevHelperNetwork.CHANNEL.sendToServer(new C2SUpdatePlayerStatsPacket(C2SUpdatePlayerStatsPacket.Action.HEAL_FULL, 0));
                showStatus("已成功回复玩家状态！");
            }).bounds(guiLeft + 20, guiTop + 50, 160, 22).build());

            this.playerStatInputBox = new EditBox(this.font, guiLeft + 20, guiTop + 90, 100, 18, Component.literal("StatValue"));
            this.playerStatInputBox.setMaxLength(10);
            this.playerStatInputBox.setTextColor(0xFFFFFF);
            this.playerStatInputBox.setValue("1000");
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

        // 1. 加载所有 PMMO 技能数据到可视化界面
        for (String pmmoId : PMMO_SKILL_IDS) {
            String cnName = CHINESE_MAP.getOrDefault(pmmoId, pmmoId);
            allAttributes.add(new AttributeItem(pmmoId, cnName, null, 0.0, 0.0));
        }

        // 2. 加载所有注册的 Forge / Minecraft / 模组属性
        for (ResourceLocation loc : ForgeRegistries.ATTRIBUTES.getKeys()) {
            Attribute attribute = ForgeRegistries.ATTRIBUTES.getValue(loc);
            if (attribute != null) {
                AttributeInstance inst = player.getAttribute(attribute);
                if (inst != null) {
                    String fullId = loc.toString();
                    String cnName = getAttributeChineseName(fullId, attribute);
                    allAttributes.add(new AttributeItem(fullId, cnName, attribute, inst.getValue(), inst.getBaseValue()));
                }
            }
        }
        filterList();
    }

    private String getAttributeChineseName(String fullId, Attribute attribute) {
        if (CHINESE_MAP.containsKey(fullId)) {
            return CHINESE_MAP.get(fullId);
        }
        try {
            Component nameComp = Component.translatable(attribute.getDescriptionId());
            String text = nameComp.getString();
            if (!text.isEmpty() && !text.equals(attribute.getDescriptionId())) {
                return text;
            }
        } catch (Exception ignored) {}

        String path = fullId.contains(":") ? fullId.split(":")[1] : fullId;
        return path.replace("generic.", "").replace("player.", "").replace("_", " ");
    }

    private void filterList() {
        filteredAttributes.clear();
        String query = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";
        for (AttributeItem item : allAttributes) {
            if (query.isEmpty() || item.id.toLowerCase(Locale.ROOT).contains(query) || item.chineseName.toLowerCase(Locale.ROOT).contains(query)) {
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
            showStatus("已成功提交 " + selectedAttribute.chineseName + " -> " + (long)val + " (支持任意无上限输入)!");
            refreshAttributeList();
        } catch (NumberFormatException e) {
            showStatus("请输入有效的数值！");
        }
    }

    private void addValue(double add) {
        if (selectedAttribute == null) return;
        double next = selectedAttribute.baseValue + add;
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.0f", next));
        applyValue();
    }

    private void setValueDirectly(double val) {
        if (selectedAttribute == null) return;
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.0f", val));
        applyValue();
    }

    private void resetDefaultValue() {
        if (selectedAttribute == null) return;
        double defaultVal = (selectedAttribute.attribute != null) ? selectedAttribute.attribute.getDefaultValue() : 1.0;
        if (valueInputBox != null) valueInputBox.setValue(String.format(Locale.ROOT, "%.2f", defaultVal));
        applyValue();
    }

    private void showStatus(String msg) {
        this.statusMessage = msg;
        this.statusTimer = System.currentTimeMillis() + 3500;
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
        int guiLeft = centerX - 195;
        int guiTop = 15;

        if (this.activeTab == 0 && button == 0) {
            int listTop = guiTop + 55;
            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredAttributes.size()) break;
                int itemY = listTop + i * ITEM_HEIGHT;
                if (mouseX >= guiLeft + 10 && mouseX <= guiLeft + 380 && mouseY >= itemY && mouseY <= itemY + ITEM_HEIGHT - 2) {
                    this.selectedAttribute = filteredAttributes.get(index);
                    if (this.valueInputBox != null) {
                        this.valueInputBox.setValue(String.format(Locale.ROOT, "%.0f", selectedAttribute.baseValue > 0 ? selectedAttribute.baseValue : 1000));
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
        int guiLeft = centerX - 195;
        int guiTop = 15;

        // 背景深色主面板 (390 x 240)
        graphics.fill(guiLeft, guiTop, guiLeft + 390, guiTop + 240, 0xF514141E);
        graphics.renderOutline(guiLeft, guiTop, 390, 240, 0xFF4A4A66);

        // 标题
        graphics.drawString(this.font, Component.literal("🛠️ 开发者辅助"), guiLeft + 12, guiTop + 10, 0xFFFFFF);

        if (this.activeTab == 0) {
            // 输入框焦点与醒目白色/青色发光边框绘制 (彻底解决“鼠标光标闪烁不明显”问题)
            if (this.searchBox != null && this.searchBox.isFocused()) {
                graphics.renderOutline(guiLeft + 9, guiTop + 31, 372, 20, 0xFF00E5FF);
            }
            if (this.valueInputBox != null && this.valueInputBox.isFocused()) {
                graphics.renderOutline(guiLeft + 9, guiTop + 207, 112, 20, 0xFF00E5FF);
            }

            // 绘制属性列表区域背景
            int listTop = guiTop + 55;
            int listHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            graphics.fill(guiLeft + 8, listTop - 2, guiLeft + 382, listTop + listHeight + 2, 0xFF0C0C12);
            graphics.renderOutline(guiLeft + 8, listTop - 2, 374, listHeight + 4, 0xFF2E2E40);

            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredAttributes.size()) break;

                AttributeItem item = filteredAttributes.get(index);
                int itemY = listTop + i * ITEM_HEIGHT;
                boolean isSelected = (selectedAttribute != null && selectedAttribute.id.equals(item.id));

                int bgColor = isSelected ? 0xFF2D4E8A : (i % 2 == 0 ? 0xFF141420 : 0xFF191928);
                graphics.fill(guiLeft + 10, itemY, guiLeft + 380, itemY + ITEM_HEIGHT - 2, bgColor);

                if (isSelected) {
                    graphics.renderOutline(guiLeft + 10, itemY, 370, ITEM_HEIGHT - 2, 0xFFFFAA00);
                }

                // 显示【中文名称】与 ID
                String titleText = item.chineseName + "  (" + item.id + ")";
                if (titleText.length() > 36) titleText = titleText.substring(0, 34) + "..";

                graphics.drawString(this.font, titleText, guiLeft + 15, itemY + 6, isSelected ? 0xFFFF55 : 0xE0E0E0);

                if (!item.id.startsWith("pmmo:")) {
                    String valStr = String.format(Locale.ROOT, "生效:%.1f | 基址:%.1f", item.currentValue, item.baseValue);
                    graphics.drawString(this.font, valStr, guiLeft + 235, itemY + 6, 0x55FF55);
                } else {
                    graphics.drawString(this.font, "支持输入任意无限大级别", guiLeft + 235, itemY + 6, 0x00E5FF);
                }
            }

            // 滚动条提示信息
            if (filteredAttributes.size() > VISIBLE_ITEMS) {
                String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + VISIBLE_ITEMS, filteredAttributes.size()) + " / " + filteredAttributes.size();
                graphics.drawString(this.font, scrollInfo, guiLeft + 310, guiTop + 36, 0x888888);
            }
        } else {
            if (this.playerStatInputBox != null && this.playerStatInputBox.isFocused()) {
                graphics.renderOutline(guiLeft + 19, guiTop + 89, 102, 20, 0xFF00E5FF);
            }
        }

        // 显示状态通知消息
        if (System.currentTimeMillis() < this.statusTimer) {
            graphics.drawString(this.font, this.statusMessage, guiLeft + 15, guiTop + 228, 0x55FF55);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
