package com.customemc.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Locale;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinTransmutationInventory {

    @Shadow
    public String filter;

    @Inject(method = "doesItemMatchFilter", at = @At("HEAD"), cancellable = true)
    private void customemc$enhancedSearchFilter(ItemInfo info, CallbackInfoReturnable<Boolean> cir) {
        if (this.filter == null || this.filter.trim().isEmpty()) {
            cir.setReturnValue(true);
            return;
        }

        String search = this.filter.toLowerCase(Locale.ROOT).trim();
        ItemStack stack = info.createStack();

        // 1. 基础 Hover 悬停展示名称匹配（原版逻辑）
        try {
            String displayName = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
            if (displayName.contains(search)) {
                cir.setReturnValue(true);
                return;
            }
        } catch (Throwable ignored) {
        }

        // 2. 物品注册 ID 匹配（例如输入 tacz / elp / modern_kinetic_gun 等搜索）
        try {
            ResourceLocation loc = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (loc != null) {
                String idStr = loc.toString().toLowerCase(Locale.ROOT);
                String pathStr = loc.getPath().toLowerCase(Locale.ROOT);
                if (idStr.contains(search) || pathStr.contains(search)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // 3. NBT 全文本深层检索（彻底解决 TACZ 枪械如 ELP-45 / elp45 等 GunId NBT 搜索）
        try {
            CompoundTag tag = info.getNBT();
            if (tag != null) {
                String nbtStr = tag.toString().toLowerCase(Locale.ROOT);
                if (nbtStr.contains(search)) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } catch (Throwable ignored) {
        }

        // 4. 自适应连字符/下划线模糊容错匹配（将 elp-45_、elp45、elp-45 等格式自动归一容错）
        try {
            String cleanSearch = search.replace("_", "").replace("-", "").replace(" ", "");
            if (!cleanSearch.isEmpty()) {
                CompoundTag tag = info.getNBT();
                if (tag != null) {
                    String cleanNbt = tag.toString().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
                    if (cleanNbt.contains(cleanSearch)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }

                ResourceLocation loc = BuiltInRegistries.ITEM.getKey(stack.getItem());
                if (loc != null) {
                    String cleanId = loc.toString().toLowerCase(Locale.ROOT).replace("_", "").replace("-", "").replace(" ", "");
                    if (cleanId.contains(cleanSearch)) {
                        cir.setReturnValue(true);
                        return;
                    }
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
