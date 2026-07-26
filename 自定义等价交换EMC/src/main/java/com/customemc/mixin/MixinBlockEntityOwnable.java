package com.customemc.mixin;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "cool.furry.mc.forge.projectexpansion.block.entity.BlockEntityOwnable", remap = false)
public class MixinBlockEntityOwnable {

    // 🚀 防护 1：当 load(CompoundTag) 传入 null 时取消加载，防止空指针断开连接闪退
    @Inject(method = "m_142466_", at = @At("HEAD"), cancellable = true)
    private void customemc$preventNullLoad(CompoundTag tag, CallbackInfo ci) {
        if (tag == null) {
            ci.cancel();
        }
    }

    // 🚀 防护 2：当 getUpdateTag() 返回 null 时自动替换为空 CompoundTag
    @Inject(method = "m_5995_", at = @At("RETURN"), cancellable = true)
    private void customemc$preventNullUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(new CompoundTag());
        }
    }
}
