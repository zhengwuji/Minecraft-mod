package com.antigravity.debuglogger.mixin;

import net.blay09.mods.balm.api.event.client.screen.ScreenInitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.blay09.mods.trashslot.client.TrashSlotGuiHandler", remap = false)
public class MixinTrashSlotFix {

    @Inject(method = "onScreenInit", at = @At("HEAD"), cancellable = true)
    private static void safeOnScreenInit(ScreenInitEvent.Post event, CallbackInfo ci) {
        // 彻底熔断 TrashSlotGuiHandler.onScreenInit，避免其在 Balm 7.3.9 下调用缺失的 isUnbound() 引发 NoSuchMethodError 崩溃！
        ci.cancel();
    }
}
