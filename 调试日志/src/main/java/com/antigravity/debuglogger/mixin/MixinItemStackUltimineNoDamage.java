package com.antigravity.debuglogger.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class MixinItemStackUltimineNoDamage {

    @Inject(method = "hurtAndBreak", at = @At("HEAD"), cancellable = true)
    private <T extends LivingEntity> void debuglogger$preventUltimineToolDamage(int amount, T entity, Consumer<T> onBroken, CallbackInfo ci) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement elem : stackTrace) {
            String className = elem.getClassName();
            if (className.contains("ftbultimine") || className.contains("inventorypets")) {
                ci.cancel();
                return;
            }
        }
    }
}
