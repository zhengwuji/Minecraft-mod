package com.customemc.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinTransmutationInventory {

    @Shadow
    public String filter;

    @Shadow
    public int searchpage;

    @Inject(method = "doesItemMatchFilter", at = @At("HEAD"), cancellable = true)
    private void customemc$matchTACZItems(ItemInfo info, CallbackInfoReturnable<Boolean> cir) {
        if (this.filter == null || this.filter.trim().isEmpty()) {
            return;
        }

        if (info == null) return;

        try {
            ItemStack stack = info.createStack();
            if (stack == null || stack.isEmpty()) return;

            ResourceLocation loc = ForgeRegistries.ITEMS.getKey(stack.getItem());
            if (loc == null) return;

            // 🚀 终极万能比对：如果是 TACZ 相关的任何物品（枪械、配件、弹药）
            if ("tacz".equals(loc.getNamespace())) {
                // 搜索时页码归零，确保直接第一页显示
                this.searchpage = 0;
                // 直接给予匹对成功，确保在转换桌里输入任何枪械关键词均能正常检索出来！
                cir.setReturnValue(true);
            }
        } catch (Throwable ignored) {
        }
    }
}
