package com.antigravity.debuglogger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.blay09.mods.trashslot.client.TrashSlotGuiHandler", remap = false)
public class MixinTrashSlotFix {

    @Inject(method = "onScreenInit", at = @At("HEAD"), cancellable = true)
    private static void safeOnScreenInit(Object event, CallbackInfo ci) {
        // 拦截 TrashSlotGuiHandler.onScreenInit，防止其调用 Balm API 缺失的 isUnbound() 方法导致加载崩溃
        ci.cancel();
    }
}
