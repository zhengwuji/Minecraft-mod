package com.antigravity.debuglogger.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.nbt.NBTManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = NBTManager.class, remap = false)
public class MixinProjectENBTManager {

    @Inject(method = "getPersistentInfo", at = @At("HEAD"), cancellable = true)
    private static void debuglogger$preserveAllNBT(ItemInfo info, CallbackInfoReturnable<ItemInfo> cir) {
        if (info != null && info.hasNBT()) {
            // 🚀 王炸级物理防御：只要物品携带 NBT，强制原封不动保留 NBT，绝对禁止 ProjectE 擦除！
            cir.setReturnValue(info);
        }
    }
}
