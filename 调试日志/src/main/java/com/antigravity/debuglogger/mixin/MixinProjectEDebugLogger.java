package com.antigravity.debuglogger.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
        // 🚀 核心大绝杀：当搜索框进行过滤更新时，强制归零页码（searchpage = 0），彻底解决翻页超界导致的搜索界面空白问题！
        if (this.filter != null && !this.filter.trim().isEmpty()) {
            this.searchpage = 0;
        }
    }

    @Inject(method = "doesItemMatchFilter", at = @At("HEAD"), cancellable = true)
    private void debuglogger$logAndMatchFilter(ItemInfo info, CallbackInfoReturnable<Boolean> cir) {
        if (this.filter == null || this.filter.trim().isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        String search = this.filter.toLowerCase(Locale.ROOT).trim();
        ItemStack stack = info.createStack();
        CompoundTag nbt = info.getNBT();

        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String itemIdStr = loc != null ? loc.toString() : "unknown";
        String hoverName = "unknown";
        try {
            hoverName = stack.getHoverName().getString();
        } catch (Throwable ignored) {
        }

        String nbtText = nbt != null ? nbt.toString() : "";

        // 多维深度与模糊容错比对
        boolean matched = false;

        // 1. 悬停展示名比对
        if (hoverName.toLowerCase(Locale.ROOT).contains(search)) {
            matched = true;
        }

        // 2. 物品注册 ID 比对
        if (!matched && (itemIdStr.toLowerCase(Locale.ROOT).contains(search) || (loc != null && loc.getPath().toLowerCase(Locale.ROOT).contains(search)))) {
            matched = true;
        }

        // 3. NBT 全文本深层比对（如 GunId）
        if (!matched && nbt != null && nbtText.toLowerCase(Locale.ROOT).contains(search)) {
            matched = true;
        }

        // 4. 清洗下划线与连字符容错比对
        if (!matched) {
            String cleanSearch = search.replace("_", "").replace("-", "").replace(" ", "");
            if (!cleanSearch.isEmpty()) {
                if (nbt != null && nbtText.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "").contains(cleanSearch)) {
                    matched = true;
                } else if (itemIdStr.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "").contains(cleanSearch)) {
                    matched = true;
                }
            }
        }

        LOGGER.info("[转换桌调试日志-HUNTER] 搜索: '{}' | 物品: {} | 悬停: '{}' | 结果: {}", search, itemIdStr, hoverName, matched);
        cir.setReturnValue(matched);
    }
}
