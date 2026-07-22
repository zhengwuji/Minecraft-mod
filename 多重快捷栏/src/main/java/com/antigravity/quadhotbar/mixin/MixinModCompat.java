package com.antigravity.quadhotbar.mixin;

import net.p3pp3rf1y.sophisticatedcore.init.ModCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = ModCompat.class, remap = false)
public class MixinModCompat {

    @Inject(method = "initCompats", at = @At("HEAD"), cancellable = true)
    private static void quadhotbar$safeInitCompats(CallbackInfo ci) {
        try {
            Class<?> modCompatClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.init.ModCompat");
            java.lang.reflect.Field factoriesField = modCompatClass.getDeclaredField("compatFactories");
            factoriesField.setAccessible(true);
            Map<?, ?> factories = (Map<?, ?>) factoriesField.get(null);

            if (factories != null) {
                for (Map.Entry<?, ?> entry : factories.entrySet()) {
                    String modId = (String) entry.getKey();
                    if (net.minecraftforge.fml.ModList.get().isLoaded(modId)) {
                        try {
                            java.util.function.Supplier<?> supplier = (java.util.function.Supplier<?>) entry.getValue();
                            java.util.concurrent.Callable<?> callable = (java.util.concurrent.Callable<?>) supplier.get();
                            Object compatObj = callable.call();
                            java.lang.reflect.Method setupMethod = compatObj.getClass().getMethod("setup");
                            setupMethod.invoke(compatObj);
                        } catch (Throwable t) {
                            org.apache.logging.log4j.LogManager.getLogger("QuadHotbar")
                                    .error("Safely bypassed error initializing compat for mod: " + modId, t);
                        }
                    }
                }
            }
            ci.cancel();
        } catch (Throwable t) {
            org.apache.logging.log4j.LogManager.getLogger("QuadHotbar").error("Error in safeInitCompats", t);
        }
    }
}
