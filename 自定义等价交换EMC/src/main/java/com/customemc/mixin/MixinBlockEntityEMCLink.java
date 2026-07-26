package com.customemc.mixin;

import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {
        "cool.furry.mc.forge.projectexpansion.block.entity.BlockEntityOwnable",
        "cool.furry.mc.forge.projectexpansion.block.entity.BlockEntityNBTFilterable",
        "cool.furry.mc.forge.projectexpansion.block.entity.BlockEntityEMCLink",
        "cool.furry.mc.forge.projectexpansion.block.entity.BlockEntityPowerFlower"
}, remap = false)
public class MixinBlockEntityEMCLink {

    // 🚀 全面防护 1：防范 load(CompoundTag nbt) 中 nbt 为 null
    @Inject(method = "m_142466_", at = @At("HEAD"), cancellable = true)
    private void customemc$preventNullLoad(CompoundTag tag, CallbackInfo ci) {
        if (tag == null) {
            ci.cancel();
        }
    }

    // 🚀 全面防护 2：防范 saveAdditional(CompoundTag nbt) 中 nbt 为 null
    @Inject(method = "m_183515_", at = @At("HEAD"), cancellable = true)
    private void customemc$preventNullSave(CompoundTag tag, CallbackInfo ci) {
        if (tag == null) {
            ci.cancel();
        }
    }

    // 🚀 全面防护 3：防范 getUpdateTag() 返回 null
    @Inject(method = "m_5995_", at = @At("RETURN"), cancellable = true)
    private void customemc$preventNullUpdateTag(CallbackInfoReturnable<CompoundTag> cir) {
        if (cir.getReturnValue() == null) {
            cir.setReturnValue(new CompoundTag());
        }
    }
}
