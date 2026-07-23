package com.antigravity.debuglogger.mixin;

import net.p3pp3rf1y.sophisticatedcore.init.ModCompat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModCompat.class, remap = false)
public class MixinModCompat {
    private static final Logger LOGGER = LogManager.getLogger("DebugLogger");

    @Inject(method = "initCompats", at = @At("HEAD"), cancellable = true)
    private static void debuglogger$safelyInitCompats(CallbackInfo ci) {
        ci.cancel();
        LOGGER.info("[DebugLogger-HUNTER] 🛡️ 全自动化接管 SophisticatedCore 兼容层初始化，提供 Throwable 级保护...");

        try {
            java.lang.reflect.Field field = ModCompat.class.getDeclaredField("compatFactories");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<String, java.util.function.Supplier<java.util.concurrent.Callable<?>>> map =
                    (java.util.Map<String, java.util.function.Supplier<java.util.concurrent.Callable<?>>>) field.get(null);

            if (map != null) {
                for (java.util.Map.Entry<String, java.util.function.Supplier<java.util.concurrent.Callable<?>>> entry : map.entrySet()) {
                    String modId = entry.getKey();
                    if (net.minecraftforge.fml.ModList.get().isLoaded(modId)) {
                        try {
                            java.util.concurrent.Callable<?> callable = entry.getValue().get();
                            Object compat = callable.call();
                            compat.getClass().getMethod("setup").invoke(compat);
                            LOGGER.info("[DebugLogger-HUNTER] 成功加载并初始化 SophisticatedCore 与 [{}] 的兼容模块", modId);
                        } catch (Throwable t) {
                            LOGGER.warn("[DebugLogger-HUNTER] 🚨 成功拦截 SophisticatedCore 与 [{}] 兼容模块的类缺失崩溃(NoClassDefFoundError)，已安全跳过:", modId, t);
                        }
                    }
                }
            }
        } catch (Throwable t) {
            LOGGER.error("[DebugLogger-HUNTER] 兼容层初始化捕获过程中的通用警告:", t);
        }
    }
}
