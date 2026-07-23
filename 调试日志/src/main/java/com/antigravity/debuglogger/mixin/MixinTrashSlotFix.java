package com.antigravity.debuglogger.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.blay09.mods.trashslot.client.TrashSlotGuiHandler", remap = false)
public class MixinTrashSlotFix {

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/blay09/mods/kuma/api/ManagedKeyMapping;isUnbound()Z"),
            require = 0
    )
    private static boolean redirectIsUnbound(Object managedKeyMapping) {
        if (managedKeyMapping == null) return true;
        try {
            java.lang.reflect.Method m = managedKeyMapping.getClass().getMethod("isUnbound");
            return (Boolean) m.invoke(managedKeyMapping);
        } catch (Throwable t) {
            try {
                java.lang.reflect.Method mKey = managedKeyMapping.getClass().getMethod("getKeyMapping");
                Object keyMapping = mKey.invoke(managedKeyMapping);
                if (keyMapping instanceof KeyMapping km) {
                    return km.isUnbound();
                }
            } catch (Throwable ignored) {}
            return false;
        }
    }
}
