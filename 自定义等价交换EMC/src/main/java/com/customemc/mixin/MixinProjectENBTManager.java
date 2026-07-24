package com.customemc.mixin;

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
    private static void customemc$preserveGunNBT(ItemInfo info, CallbackInfoReturnable<ItemInfo> cir) {
        if (info != null && info.hasNBT()) {
            try {
                // 🚀 世纪病根终杀：如果物品包含 GunId 或属于 TACZ 枪械，强制原封不动保留 NBT，绝对禁止 ProjectE 擦除 GunId！
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
