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

import java.util.Map;

public class CustomEMCScreen extends Screen {

    private EditBox defaultEmcBox;
    private EditBox itemIdBox;
    private EditBox customPriceBox;

    private String statusMessage = "";
    private int statusColor = 0xFF55FF55;

    public CustomEMCScreen() {
        super(Component.m_237115_("自定义等价交换EMC 控制面板"));
    }

    @Override
    protected void m_7856_() {
        super.m_7856_();

        int panelWidth = 320;
        int panelHeight = 210;
        int left = (this.f_96543_ - panelWidth) / 2;
        int top = (this.f_96544_ - panelHeight) / 2;

        ConfigManager.loadConfig();

        this.defaultEmcBox = new EditBox(this.f_96547_, left + 155, top + 42, 140, 18, Component.m_237115_("默认EMC"));
        this.defaultEmcBox.m_94144_(String.valueOf(ConfigManager.getDefaultEMC()));
        this.m_142416_(this.defaultEmcBox);

        this.itemIdBox = new EditBox(this.f_96547_, left + 20, top + 95, 170, 18, Component.m_237115_("物品ID"));
        this.itemIdBox.m_94144_("minecraft:dirt");
        this.m_142416_(this.itemIdBox);

        this.m_142416_(Button.m_253074_(Component.m_237115_("抓取主手物品"), btn -> {
            if (this.f_96541_ != null && this.f_96541_.f_91074_ != null) {
                ItemStack mainHand = this.f_96541_.f_91074_.m_21205_();
                if (!mainHand.m_41619_()) {
                    ResourceLocation loc = ForgeRegistries.ITEMS.getKey(mainHand.m_41720_());
                    if (loc != null) {
                        this.itemIdBox.m_94144_(loc.toString());
                        this.statusMessage = "已抓取主手物品: " + loc;
                        this.statusColor = 0xFF55FFFF;
                    }
                } else {
                    this.statusMessage = "主手未持有任何物品！";
                    this.statusColor = 0xFFFF5555;
                }
            }
        }).m_252987_(left + 200, top + 94, 100, 20).m_253136_());

        this.customPriceBox = new EditBox(this.f_96547_, left + 20, top + 135, 100, 18, Component.m_237115_("指定价格"));
        this.customPriceBox.m_94144_("1");
        this.m_142416_(this.customPriceBox);

        this.m_142416_(Button.m_253074_(Component.m_237115_("添加/更新此指定价格"), btn -> {
            String itemStr = this.itemIdBox.m_94155_().trim();
            String priceStr = this.customPriceBox.m_94155_().trim();
            try {
                long price = Long.parseLong(priceStr);
                if (itemStr.isEmpty() || price < 0) {
                    this.statusMessage = "物品ID为空或价格非法！";
                    this.statusColor = 0xFFFF5555;
                    return;
                }
                Map<String, Long> map = ConfigManager.getCustomEMCMap();
                map.put(itemStr, price);
                this.statusMessage = "已修改指定项: " + itemStr + " -> " + price;
                this.statusColor = 0xFF55FF55;
            } catch (Exception e) {
                this.statusMessage = "价格必须为正整数！";
                this.statusColor = 0xFFFF5555;
            }
        }).m_252987_(left + 130, top + 134, 170, 20).m_253136_());

        this.m_142416_(Button.m_253074_(Component.m_237115_("保存配置并同步重载 EMC"), btn -> {
            try {
                long defVal = Long.parseLong(this.defaultEmcBox.m_94155_().trim());
                Map<String, Long> map = ConfigManager.getCustomEMCMap();
                ConfigManager.saveConfig(defVal, map);

                if (this.f_96541_ != null && this.f_96541_.f_91074_ != null) {
                    this.f_96541_.f_91074_.m_108748_("/projecte reloadEMC");
                }
                this.statusMessage = "保存成功！已自动发送命令重载 EMC！";
                this.statusColor = 0xFF55FF55;
            } catch (Exception e) {
                this.statusMessage = "保存失败，请检查数值！";
                this.statusColor = 0xFFFF5555;
            }
        }).m_252987_(left + 20, top + 175, 190, 22).m_253136_());

        this.m_142416_(Button.m_253074_(Component.m_237115_("关闭 (Esc)"), btn -> {
            this.m_7379_();
        }).m_252987_(left + 220, top + 175, 80, 22).m_253136_());
    }

    @Override
    public void m_88315_(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 使用全屏 50% 黑色渐变遮罩渲染背景，避免调用触发 Mixin 递归调用的 renderBackground
        guiGraphics.m_280509_(0, 0, this.f_96543_, this.f_96544_, 0x80000000);

        int panelWidth = 320;
        int panelHeight = 210;
        int left = (this.f_96543_ - panelWidth) / 2;
        int top = (this.f_96544_ - panelHeight) / 2;

        guiGraphics.m_280509_(left, top, left + panelWidth, top + panelHeight, 0xE010141D);
        guiGraphics.m_280046_(left, top, left + panelWidth, top + 25, 0xFF2C3E50, 0xFF1A252F);

        guiGraphics.m_280509_(left, top, left + panelWidth, top + 1, 0xFFF39C12);
        guiGraphics.m_280509_(left, top + panelHeight - 1, left + panelWidth, top + panelHeight, 0xFFF39C12);
        guiGraphics.m_280509_(left, top, left + 1, top + panelHeight, 0xFFF39C12);
        guiGraphics.m_280509_(left + panelWidth - 1, top, left + panelWidth, top + panelHeight, 0xFFF39C12);

        guiGraphics.m_280056_(this.f_96547_, "⚡ 自定义等价交换 EMC 控制面板 (F8)", left + 12, top + 8, 0xFFFFFF00, true);

        guiGraphics.m_280056_(this.f_96547_, "全局默认EMC (无价格物品):", left + 20, top + 46, 0xFFE0E0E0, true);
        guiGraphics.m_280056_(this.f_96547_, "指定物品 EMC 覆盖设置:", left + 20, top + 80, 0xFF3498DB, true);
        guiGraphics.m_280056_(this.f_96547_, "指定EMC价格:", left + 20, top + 120, 0xFFE0E0E0, true);

        if (this.statusMessage != null && !this.statusMessage.isEmpty()) {
            guiGraphics.m_280056_(this.f_96547_, this.statusMessage, left + 20, top + 160, this.statusColor, true);
        }

        super.m_88315_(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean m_6913_() {
        return false;
    }
}
