package com.antigravity.debuglogger.mixin;

import net.blay09.mods.kuma.api.ManagedKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.blay09.mods.trashslot.client.TrashSlotGuiHandler", remap = false)
public class MixinTrashSlotFix {

    @Redirect(
            method = "onScreenInit",
            at = @At(value = "INVOKE", target = "Lnet/blay09/mods/kuma/api/ManagedKeyMapping;isUnbound()Z"),
            require = 0
    )
    private static boolean redirectIsUnbound(ManagedKeyMapping keyMapping) {
        if (keyMapping == null) return true;
        try {
            return keyMapping.isUnbound();
        } catch (Throwable t) {
            // 当 Kuma API 缺少 isUnbound() 方法时，静默捕获异常并返回 false (代表快捷键生效)
            // 使得 TrashSlotGuiHandler 正常继续装载并成功在物品栏右下角渲染垃圾桶槽位！
            return false;
        }
    }
}
