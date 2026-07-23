package com.antigravity.debuglogger.mixin;

import com.sammy.malum.client.renderer.block.VoidDepotRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.RenderHandler;

@Mixin(value = VoidDepotRenderer.class, remap = false)
public class MixinVoidDepotRenderer {

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void debuglogger$preInitCheck(CallbackInfo ci) {
        if (RenderHandler.DELAYED_RENDER == null || RenderHandler.LATE_DELAYED_RENDER == null) {
            try {
                RenderHandler.onClientSetup(null);
            } catch (Throwable ignored) {
            }
        }
    }
}
