package com.antigravity.debuglogger.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.nbt.NBTManager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NBTManager.class, remap = false)
public class MixinProjectENBTManager {

    @Inject(method = "getPersistentInfo", at = @At("HEAD"), cancellable = true)
    private static void debuglogger$preserveGunNBT(ItemInfo info, CallbackInfoReturnable<ItemInfo> cir) {
        if (info != null && info.hasNBT()) {
            try {
                // 🚀 物理级绝杀：枪械及带 GunId 的物品进入转换桌时，强制禁止 ProjectE 擦除 GunId NBT！
                if (info.getNBT().contains("GunId")) {
                    cir.setReturnValue(info);
                    return;
                }
                ItemStack stack = info.createStack();
                if (stack.getItem() instanceof com.tacz.guns.api.item.IGun) {
                    cir.setReturnValue(info);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
