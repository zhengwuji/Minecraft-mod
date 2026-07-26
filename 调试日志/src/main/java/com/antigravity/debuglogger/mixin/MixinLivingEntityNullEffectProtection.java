package com.antigravity.debuglogger.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = -10000)
public class MixinLivingEntityNullEffectProtection {

    @Inject(method = "hasEffect", at = @At("HEAD"), cancellable = true)
    private void debuglogger$preventNullMobEffectCrash(MobEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == null) {
            cir.setReturnValue(false);
        }
    }
}
