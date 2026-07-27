package com.customemc.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "com.inolia_zaicek.background_music_player.client.NowPlayingDisplay", remap = false)
public class MixinNowPlayingDisplay {
    @Inject(method = "show", at = @At("HEAD"), cancellable = true)
    private static void cancelNowPlayingToast(Object track, CallbackInfo ci) {
        ci.cancel();
    }
}
