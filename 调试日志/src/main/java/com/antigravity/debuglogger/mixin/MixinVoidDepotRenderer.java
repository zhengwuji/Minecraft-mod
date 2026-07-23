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
    private static void debuglogger$ensureLodestoneRenderLayer(CallbackInfo ci) {
        try {
            if (RenderHandler.DELAYED_RENDER == null) {
                RenderHandler.onClientSetup(null);
            }
        } catch (Throwable t) {
            try {
                if (RenderHandler.DELAYED_RENDER == null) {
                    RenderHandler.DELAYED_RENDER = new RenderHandler.LodestoneRenderLayer(
                            RenderHandler.BUFFERS, RenderHandler.PARTICLE_BUFFERS);
                }
                if (RenderHandler.LATE_DELAYED_RENDER == null) {
                    RenderHandler.LATE_DELAYED_RENDER = new RenderHandler.LodestoneRenderLayer(
                            RenderHandler.LATE_BUFFERS, RenderHandler.LATE_PARTICLE_BUFFERS);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
