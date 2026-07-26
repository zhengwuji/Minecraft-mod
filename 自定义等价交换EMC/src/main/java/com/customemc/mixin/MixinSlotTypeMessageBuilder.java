package com.customemc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "top.theillusivec4.curios.api.SlotTypeMessage$Builder", remap = false)
public class MixinSlotTypeMessageBuilder {

    @Shadow
    private String identifier;

    @Shadow
    private int size;

    // 🚀 强行解封【货币战争饰品 (currency_wars_curios 与 wallet)】槽位为 15 个！
    @Inject(method = "build", at = @At("HEAD"))
    private void customemc$unlockWalletSlotSize(CallbackInfoReturnable<?> cir) {
        if ("currency_wars_curios".equalsIgnoreCase(this.identifier) || "wallet".equalsIgnoreCase(this.identifier)) {
            this.size = 15;
        }
    }
}
