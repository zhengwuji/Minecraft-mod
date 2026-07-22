package com.antigravity.quadhotbar.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.handlers.RenderHandler;

@Mixin(value = RenderHandler.class, remap = false)
public class MixinRenderHandler {

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void quadhotbar$ensureRenderLayersInitialized(CallbackInfo ci) {
        if (RenderHandler.DELAYED_RENDER == null || RenderHandler.LATE_DELAYED_RENDER == null) {
            try {
                RenderHandler.onClientSetup(null);
            } catch (Throwable ignored) {
            }
        }
    }
}
