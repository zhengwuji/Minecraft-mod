package com.antigravity.debuglogger.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Locale;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinProjectEDebugLogger {
    private static final Logger LOGGER = LogManager.getLogger("DevDebugLogger");

    @Shadow
    public String filter;

    @Shadow
    public int searchpage;

    @Inject(method = "updateClientTargets", at = @At("HEAD"))
    private void debuglogger$resetSearchPageOnUpdate(CallbackInfo ci) {
        // 🚀 当搜索框进行过滤更新时，强制重置归零页码（searchpage = 0），彻底解决翻页超界导致的搜索界面空白问题！
        if (this.filter != null && !this.filter.trim().isEmpty()) {
            this.searchpage = 0;
        }
    }

    @Inject(method = "doesItemMatchFilter", at = @At("HEAD"), cancellable = true)
    private void debuglogger$tooltipMatchFilter(ItemInfo info, CallbackInfoReturnable<Boolean> cir) {
        if (this.filter == null || this.filter.trim().isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        String search = this.filter.toLowerCase(Locale.ROOT).trim();
        ItemStack stack = info.createStack();

        // 🚀 新思路绝杀：全量抓取物品在游戏渲染中的 Tooltip 文本（包含 ELP-45「夜莺」电浆步枪、EOSlab 曙光女神实验室 等文本）
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                List<Component> tooltipLines = stack.getTooltipLines(
                        mc.player,
                        mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL
                );
                for (Component line : tooltipLines) {
                    String lineText = line.getString().toLowerCase(Locale.ROOT);
                    if (lineText.contains(search)) {
                        LOGGER.info("[转换桌Tooltip万能搜索-MATCH] 输入: '{}' | 在 Tooltip 行 '{}' 完美匹配！", search, line.getString());
                        cir.setReturnValue(true);
                        return;
                    }

                    // 连字符与下划线清洗容错
                    String cleanSearch = search.replace("_", "").replace("-", "").replace(" ", "");
                    if (!cleanSearch.isEmpty()) {
                        String cleanLine = lineText.replace("_", "").replace("-", "").replace(" ", "");
                        if (cleanLine.contains(cleanSearch)) {
                            LOGGER.info("[转换桌Tooltip万能搜索-MATCH] 输入: '{}' | 清洗容错后在 Tooltip 行 '{}' 完美匹配！", search, line.getString());
                            cir.setReturnValue(true);
                            return;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        // 后备匹配：注册 ID 匹配
        try {
            ResourceLocation loc = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (loc != null) {
                String idStr = loc.toString().toLowerCase(Locale.ROOT);
                if (idStr.contains(search) || loc.getPath().toLowerCase(Locale.ROOT).contains(search)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // 后备匹配：NBT 匹配
        try {
            CompoundTag nbt = info.getNBT();
            if (nbt != null) {
                String nbtText = nbt.toString().toLowerCase(Locale.ROOT);
                if (nbtText.contains(search)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
