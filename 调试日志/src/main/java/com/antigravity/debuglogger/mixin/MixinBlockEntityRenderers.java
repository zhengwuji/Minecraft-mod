package com.antigravity.debuglogger.mixin;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BlockEntityRenderers.class)
public class MixinBlockEntityRenderers {
    private static final Logger LOGGER = LogManager.getLogger("DebugLogger");

    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void debuglogger$prewarmLodestoneBeforeBlockEntityRenderers(CallbackInfo ci) {
        try {
            Class<?> rhClass = Class.forName("team.lodestar.lodestone.handlers.RenderHandler");
            rhClass.getMethod("onClientSetup", net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent.class).invoke(null, (Object) null);
            LOGGER.info("[调试日志-HUNTER] 🛡️ 成功在 BlockEntityRenderers 静态初始化 HEAD 处抢先预热初始化 Lodestone RenderHandler！");
        } catch (Throwable t) {
            LOGGER.warn("[调试日志-HUNTER] 预热初始化 Lodestone RenderHandler 异常:", t);
        }
    }
}
