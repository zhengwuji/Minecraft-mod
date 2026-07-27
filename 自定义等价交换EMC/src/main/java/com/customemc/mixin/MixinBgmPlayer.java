package com.customemc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.inolia_zaicek.background_music_player.client.BgmPlayer", remap = false)
public class MixinBgmPlayer {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true, require = 0)
    private static void cancelBgmPlayerTick(CallbackInfo ci) {
        ci.cancel();
    }
}
