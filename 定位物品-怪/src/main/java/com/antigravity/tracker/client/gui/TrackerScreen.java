package com.antigravity.tracker.client.gui;

import com.antigravity.tracker.config.TrackerConfig;
import com.antigravity.tracker.util.ChineseNameMapper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class TrackerScreen extends Screen {
    private EditBox searchBox;
    private EditBox customDistBox;
    private int activeTab = 0; // 0: 全局, 1: 已开启列表, 2: 怪物实体, 3: 方块矿石, 4: 物品掉落, 5: 设置

    private final List<TargetItem> currentList = new ArrayList<>();
    private final List<TargetItem> filteredList = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 24;
    private static final int VISIBLE_ITEMS = 6;

    public static class TargetItem {
        public final String id;
        public final String chineseName;
        public final int category; // 0: entity, 1: block, 2: item

        public TargetItem(String id, String chineseName, int category) {
            this.id = id;
            this.chineseName = chineseName;
            this.category = category;
        }

        public String getCategoryBadge() {
            if (category == 0) return "[👾怪]";
            if (category == 1) return "[📦块]";
            return "[💎物]";
        }
    }

    public TrackerScreen() {
        super(Component.literal("🎯 定位物品-怪 追踪配置面板"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int guiLeft = centerX - 195;
        int guiTop = 15;

        // 顶部 6 大功能页签按钮
        this.addRenderableWidget(Button.builder(Component.literal("🔍 全局"), btn -> switchTab(0))
                .bounds(guiLeft + 5, guiTop + 6, 55, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("✅ 已开启"), btn -> switchTab(1))
                .bounds(guiLeft + 65, guiTop + 6, 65, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("👾 怪物"), btn -> switchTab(2))
                .bounds(guiLeft + 135, guiTop + 6, 55, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("📦 矿石/方块"), btn -> switchTab(3))
                .bounds(guiLeft + 195, guiTop + 6, 75, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("💎 物品"), btn -> switchTab(4))
                .bounds(guiLeft + 275, guiTop + 6, 55, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("⚙️ 设置"), btn -> switchTab(5))
                .bounds(guiLeft + 335, guiTop + 6, 50, 20).build());

        if (this.activeTab != 5) {
            // 搜索框
            this.searchBox = new EditBox(this.font, guiLeft + 10, guiTop + 32, 370, 18, Component.literal("Search"));
            this.searchBox.setMaxLength(100);
            this.searchBox.setHint(Component.literal("🔍 输入名称或ID (如 宝箱怪, 宝箱, 僵尸, 钻石, mimic, zombie)..."));
            this.searchBox.setTextColor(0xFFFFFF);
            this.searchBox.setResponder(text -> filterList());
            this.addRenderableWidget(this.searchBox);

            if (this.activeTab == 1) {
                // 已开启列表管理界面
                this.addRenderableWidget(Button.builder(Component.literal("一键全关所有已追踪目标"), btn -> clearAllTracked())
                        .bounds(guiLeft + 10, guiTop + 208, 175, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("一键全开全箱子与宝箱怪"), btn -> enableAllChestsAndMimics())
                        .bounds(guiLeft + 190, guiTop + 208, 190, 20).build());
            } else {
                // 底部一键预设快捷按钮行
                String btnText = "一键开启筛选出的 " + filteredList.size() + " 个目标";
                this.addRenderableWidget(Button.builder(Component.literal(btnText), btn -> enableAllFiltered())
                        .bounds(guiLeft + 10, guiTop + 208, 175, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("一键全开全箱子与宝箱怪"), btn -> enableAllChestsAndMimics())
                        .bounds(guiLeft + 190, guiTop + 208, 115, 20).build());

                this.addRenderableWidget(Button.builder(Component.literal("清空当前页"), btn -> clearCurrentTab())
                        .bounds(guiLeft + 310, guiTop + 208, 70, 20).build());
            }
        } else {
            // 设置页签控制控件
            String statusText = TrackerConfig.enabled ? "全局定位追踪: [已开启]" : "全局定位追踪: [已关闭]";
            this.addRenderableWidget(Button.builder(Component.literal(statusText), btn -> {
                TrackerConfig.enabled = !TrackerConfig.enabled;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 20, guiTop + 45, 170, 22).build());

            String tracerText = TrackerConfig.showTracers ? "连接追踪射线: [显示]" : "连接追踪射线: [隐藏]";
            this.addRenderableWidget(Button.builder(Component.literal(tracerText), btn -> {
                TrackerConfig.showTracers = !TrackerConfig.showTracers;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 200, guiTop + 45, 170, 22).build());

            String distText = TrackerConfig.showDistanceText ? "距离与名称悬浮标签: [显示]" : "距离与名称悬浮标签: [隐藏]";
            this.addRenderableWidget(Button.builder(Component.literal(distText), btn -> {
                TrackerConfig.showDistanceText = !TrackerConfig.showDistanceText;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 20, guiTop + 80, 170, 22).build());

            // 半径预设按钮
            this.addRenderableWidget(Button.builder(Component.literal("128m"), btn -> {
                TrackerConfig.maxDistance = 128.0;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 20, guiTop + 120, 65, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("512m"), btn -> {
                TrackerConfig.maxDistance = 512.0;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 90, guiTop + 120, 65, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("1000m"), btn -> {
                TrackerConfig.maxDistance = 1000.0;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 160, guiTop + 120, 65, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("2000m"), btn -> {
                TrackerConfig.maxDistance = 2000.0;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 230, guiTop + 120, 65, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("5000m"), btn -> {
                TrackerConfig.maxDistance = 5000.0;
                TrackerConfig.saveConfig();
                rebuildWidgets();
            }).bounds(guiLeft + 300, guiTop + 120, 65, 20).build());

            // 自定义范围输入框
            this.customDistBox = new EditBox(this.font, guiLeft + 20, guiTop + 150, 150, 20, Component.literal("CustomDist"));
            this.customDistBox.setValue(String.valueOf((int) TrackerConfig.maxDistance));
            this.customDistBox.setHint(Component.literal("输入自定义米数"));
            this.addRenderableWidget(this.customDistBox);

            this.addRenderableWidget(Button.builder(Component.literal("设定自定义半径"), btn -> {
                try {
                    double val = Double.parseDouble(customDistBox.getValue());
                    if (val > 0) {
                        TrackerConfig.maxDistance = val;
                        TrackerConfig.saveConfig();
                    }
                } catch (Exception ignored) {}
                rebuildWidgets();
            }).bounds(guiLeft + 180, guiTop + 150, 110, 20).build());
        }

        loadTabItems();
    }

    private void switchTab(int tab) {
        this.activeTab = tab;
        rebuildWidgets();
    }

    private void loadTabItems() {
        currentList.clear();

        if (activeTab == 1) {
            // ✅ 已开启列表专有页签：加载所有当前开启追踪的目标
            TrackerConfig.trackedEntities.forEach((id, color) -> {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(id));
                String name = ChineseNameMapper.getEntityName(type, id);
                currentList.add(new TargetItem(id, name, 0));
            });

            TrackerConfig.trackedBlocks.forEach((id, color) -> {
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
                String name = ChineseNameMapper.getBlockName(block, id);
                currentList.add(new TargetItem(id, name, 1));
            });

            TrackerConfig.trackedItems.forEach((id, color) -> {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(id));
                String name = ChineseNameMapper.getItemName(item, id);
                currentList.add(new TargetItem(id, name, 2));
            });
        } else {
            if (activeTab == 0 || activeTab == 2) {
                // 加载实体/怪物注册表
                for (ResourceLocation loc : ForgeRegistries.ENTITY_TYPES.getKeys()) {
                    EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(loc);
                    if (type != null) {
                        String fullId = loc.toString();
                        String cnName = ChineseNameMapper.getEntityName(type, fullId);
                        currentList.add(new TargetItem(fullId, cnName, 0));
                    }
                }
            }
            if (activeTab == 0 || activeTab == 3) {
                // 加载方块/矿石注册表
                for (ResourceLocation loc : ForgeRegistries.BLOCKS.getKeys()) {
                    Block block = ForgeRegistries.BLOCKS.getValue(loc);
                    if (block != null) {
                        String fullId = loc.toString();
                        String cnName = ChineseNameMapper.getBlockName(block, fullId);
                        currentList.add(new TargetItem(fullId, cnName, 1));
                    }
                }
            }
            if (activeTab == 0 || activeTab == 4) {
                // 加载物品/掉落物注册表
                for (ResourceLocation loc : ForgeRegistries.ITEMS.getKeys()) {
                    Item item = ForgeRegistries.ITEMS.getValue(loc);
                    if (item != null) {
                        String fullId = loc.toString();
                        String cnName = ChineseNameMapper.getItemName(item, fullId);
                        currentList.add(new TargetItem(fullId, cnName, 2));
                    }
                }
            }
        }
        filterList();
    }

    private void filterList() {
        filteredList.clear();
        String query = searchBox != null ? searchBox.getValue().toLowerCase(Locale.ROOT).trim() : "";
        for (TargetItem item : currentList) {
            if (query.isEmpty() || item.id.toLowerCase(Locale.ROOT).contains(query) || item.chineseName.toLowerCase(Locale.ROOT).contains(query)) {
                filteredList.add(item);
            }
        }
        this.scrollOffset = 0;
    }

    private void enableAllFiltered() {
        for (TargetItem item : filteredList) {
            setTrackedGrouped(item, true, getColor(item));
        }
        TrackerConfig.saveConfig();
        rebuildWidgets();
    }

    private void enableAllChestsAndMimics() {
        // 开启怪物宝箱怪
        String[] mimics = {
                "artifacts:mimic", "faded_conquest_2:mimic", "grimoireofgaia:mimic", "aether:mimic", "mowziesmobs:mimic"
        };
        for (String id : mimics) {
            TrackerConfig.toggleEntity(id, true, 0xFFFF0000);
        }

        // 开启方块箱子类与战利品箱
        String[] chests = {
                "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel",
                "lootr:lootr_chest", "lootr:lootr_trapped_chest", "lootr:lootr_barrel", "lootr:lootr_inventory",
                "sophisticatedstorage:chest", "sophisticatedstorage:barrel",
                "aquamirae:frozen_chest", "avaritia:compressed_chest"
        };
        for (String id : chests) {
            TrackerConfig.toggleBlock(id, true, 0xFFFFFF00);
        }
        TrackerConfig.saveConfig();
        rebuildWidgets();
    }

    private void clearAllTracked() {
        TrackerConfig.trackedEntities.clear();
        TrackerConfig.trackedBlocks.clear();
        TrackerConfig.trackedItems.clear();
        TrackerConfig.saveConfig();
        rebuildWidgets();
    }

    private void clearCurrentTab() {
        if (activeTab == 0 || activeTab == 1) {
            clearAllTracked();
        } else if (activeTab == 2) {
            TrackerConfig.trackedEntities.clear();
        } else if (activeTab == 3) {
            TrackerConfig.trackedBlocks.clear();
        } else if (activeTab == 4) {
            TrackerConfig.trackedItems.clear();
        }
        TrackerConfig.saveConfig();
        rebuildWidgets();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (this.activeTab != 5 && !filteredList.isEmpty()) {
            if (delta < 0 && scrollOffset < filteredList.size() - VISIBLE_ITEMS) {
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

        if (this.activeTab != 5 && button == 0) {
            int listTop = guiTop + 55;
            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredList.size()) break;
                TargetItem item = filteredList.get(index);
                int itemY = listTop + i * ITEM_HEIGHT;

                // 判断点击 [追踪开关] 按钮区域 (guiLeft + 270 ~ guiLeft + 325)
                if (mouseX >= guiLeft + 270 && mouseX <= guiLeft + 325 && mouseY >= itemY + 2 && mouseY <= itemY + ITEM_HEIGHT - 4) {
                    boolean tracked = isTracked(item);
                    int curColor = getColor(item);
                    setTrackedGrouped(item, !tracked, curColor);
                    rebuildWidgets();
                    return true;
                }

                // 判断点击 [切换颜色] 按钮区域 (guiLeft + 330 ~ guiLeft + 375)
                if (mouseX >= guiLeft + 330 && mouseX <= guiLeft + 375 && mouseY >= itemY + 2 && mouseY <= itemY + ITEM_HEIGHT - 4) {
                    int curColor = getColor(item);
                    int nextColor = TrackerConfig.getNextColor(curColor);
                    setTrackedGrouped(item, true, nextColor);
                    rebuildWidgets();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isTracked(TargetItem item) {
        if (item.category == 0) return TrackerConfig.trackedEntities.containsKey(item.id);
        if (item.category == 1) return TrackerConfig.trackedBlocks.containsKey(item.id);
        return TrackerConfig.trackedItems.containsKey(item.id);
    }

    private int getColor(TargetItem item) {
        if (item.category == 0) return TrackerConfig.getEntityColor(item.id);
        if (item.category == 1) return TrackerConfig.getBlockColor(item.id);
        return TrackerConfig.getItemColor(item.id);
    }

    private void setTrackedGrouped(TargetItem item, boolean track, int color) {
        String lowerId = item.id.toLowerCase(Locale.ROOT);
        String lowerName = item.chineseName.toLowerCase(Locale.ROOT);

        // 1. 判断是否为【箱子/宝箱/木桶/容器类】组 (同类自动联动全选/取消)
        boolean isChestGroup = lowerId.contains("chest") || lowerId.contains("barrel") || lowerId.contains("shulker")
                || lowerId.contains("lootr") || lowerId.contains("storage") || lowerId.contains("box")
                || lowerName.contains("箱") || lowerName.contains("宝箱");

        // 2. 判断是否为【矿石/残骸类】组
        boolean isOreGroup = lowerId.contains("ore") || lowerId.contains("debris") || lowerName.contains("矿");

        // 3. 判断是否为【僵尸/敌对怪类】组
        boolean isMonsterGroup = lowerId.contains("zombie") || lowerId.contains("skeleton") || lowerId.contains("creeper")
                || lowerId.contains("mimic") || lowerName.contains("僵尸") || lowerName.contains("骷髅")
                || lowerName.contains("苦力怕") || lowerName.contains("宝箱怪");

        if (isChestGroup) {
            // 联动全选/取消全图所有箱子类与宝箱怪
            enableAllChestsAndMimicsGroup(track, color);
        } else if (isOreGroup) {
            enableAllOresGroup(track, color);
        } else if (isMonsterGroup) {
            enableAllMonstersGroup(track, color);
        } else {
            // 普通单个目标设置
            if (item.category == 0) TrackerConfig.toggleEntity(item.id, track, color);
            else if (item.category == 1) TrackerConfig.toggleBlock(item.id, track, color);
            else TrackerConfig.toggleItem(item.id, track, color);
        }
        TrackerConfig.saveConfig();
    }

    private void enableAllChestsAndMimicsGroup(boolean track, int color) {
        String[] mimics = {
                "artifacts:mimic", "faded_conquest_2:mimic", "grimoireofgaia:mimic", "aether:mimic", "mowziesmobs:mimic"
        };
        for (String id : mimics) {
            TrackerConfig.toggleEntity(id, track, color);
        }

        String[] chests = {
                "minecraft:chest", "minecraft:trapped_chest", "minecraft:ender_chest", "minecraft:barrel",
                "lootr:lootr_chest", "lootr:lootr_trapped_chest", "lootr:lootr_barrel", "lootr:lootr_inventory",
                "sophisticatedstorage:chest", "sophisticatedstorage:barrel",
                "aquamirae:frozen_chest", "avaritia:compressed_chest"
        };
        for (String id : chests) {
            TrackerConfig.toggleBlock(id, track, color);
        }
    }

    private void enableAllOresGroup(boolean track, int color) {
        String[] ores = {
                "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore",
                "minecraft:ancient_debris", "minecraft:spawner",
                "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore",
                "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                "minecraft:iron_ore", "minecraft:deepslate_iron_ore"
        };
        for (String id : ores) {
            TrackerConfig.toggleBlock(id, track, color);
        }
    }

    private void enableAllMonstersGroup(boolean track, int color) {
        String[] hostiles = {
                "artifacts:mimic", "faded_conquest_2:mimic", "grimoireofgaia:mimic", "aether:mimic", "mowziesmobs:mimic",
                "minecraft:zombie", "minecraft:skeleton", "minecraft:creeper", "minecraft:spider",
                "minecraft:enderman", "minecraft:witch", "minecraft:phantom", "minecraft:drowned",
                "minecraft:husk", "minecraft:stray", "minecraft:wither_skeleton", "minecraft:blaze",
                "minecraft:ghast", "minecraft:ender_dragon", "minecraft:wither"
        };
        for (String id : hostiles) {
            TrackerConfig.toggleEntity(id, track, color);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int guiLeft = centerX - 195;
        int guiTop = 15;

        // 深色主面板
        graphics.fill(guiLeft, guiTop, guiLeft + 390, guiTop + 240, 0xF514141E);
        graphics.renderOutline(guiLeft, guiTop, 390, 240, 0xFF4A4A66);

        if (this.activeTab != 5) {
            // 输入框高亮焦点发光边框
            if (this.searchBox != null && this.searchBox.isFocused()) {
                graphics.renderOutline(guiLeft + 9, guiTop + 31, 372, 20, 0xFF00E5FF);
            }

            int listTop = guiTop + 55;
            int listHeight = VISIBLE_ITEMS * ITEM_HEIGHT;
            graphics.fill(guiLeft + 8, listTop - 2, guiLeft + 382, listTop + listHeight + 2, 0xFF0C0C12);
            graphics.renderOutline(guiLeft + 8, listTop - 2, 374, listHeight + 4, 0xFF2E2E40);

            for (int i = 0; i < VISIBLE_ITEMS; i++) {
                int index = scrollOffset + i;
                if (index >= filteredList.size()) break;

                TargetItem item = filteredList.get(index);
                int itemY = listTop + i * ITEM_HEIGHT;
                boolean isTracked = isTracked(item);
                int color = getColor(item);

                int bgColor = isTracked ? 0xFF1E3A20 : (i % 2 == 0 ? 0xFF141420 : 0xFF191928);
                graphics.fill(guiLeft + 10, itemY, guiLeft + 380, itemY + ITEM_HEIGHT - 2, bgColor);

                // 显示类型标识与中文名称 + 注册 ID
                String titleText = item.getCategoryBadge() + " " + item.chineseName + " (" + item.id + ")";
                if (titleText.length() > 32) titleText = titleText.substring(0, 30) + "..";
                graphics.drawString(this.font, titleText, guiLeft + 15, itemY + 6, isTracked ? 0x55FF55 : 0xCCCCCC);

                // 绘制 [开关状态按钮]
                int toggleBg = isTracked ? 0xFF28A745 : 0xFF4A4A66;
                graphics.fill(guiLeft + 270, itemY + 2, guiLeft + 325, itemY + ITEM_HEIGHT - 4, toggleBg);
                graphics.drawString(this.font, isTracked ? "已追踪" : "未开启", guiLeft + 277, itemY + 6, 0xFFFFFF);

                // 绘制 [颜色预设按钮]
                graphics.fill(guiLeft + 330, itemY + 2, guiLeft + 375, itemY + ITEM_HEIGHT - 4, color);
                graphics.renderOutline(guiLeft + 330, itemY + 2, 45, ITEM_HEIGHT - 6, 0xFFFFFFFF);
            }

            // 滚动计数器
            if (filteredList.size() > VISIBLE_ITEMS) {
                String scrollInfo = (scrollOffset + 1) + "-" + Math.min(scrollOffset + VISIBLE_ITEMS, filteredList.size()) + " / " + filteredList.size();
                graphics.drawString(this.font, scrollInfo, guiLeft + 310, guiTop + 36, 0x888888);
            }
        } else {
            // 全局设置说明
            graphics.drawString(this.font, "🎯 定位物品-怪 模组全局参数设置", guiLeft + 20, guiTop + 30, 0xFFD700);
            graphics.drawString(this.font, "当前最大透视与距离追踪半径: " + (int)TrackerConfig.maxDistance + " 方块", guiLeft + 20, guiTop + 105, 0x00E5FF);
            graphics.drawString(this.font, "已激活实体追踪目标数: " + TrackerConfig.trackedEntities.size(), guiLeft + 20, guiTop + 178, 0x55FF55);
            graphics.drawString(this.font, "已激活方块追踪目标数: " + TrackerConfig.trackedBlocks.size(), guiLeft + 20, guiTop + 194, 0xFFFF55);
            graphics.drawString(this.font, "已激活物品追踪目标数: " + TrackerConfig.trackedItems.size(), guiLeft + 20, guiTop + 210, 0xFF55FF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
