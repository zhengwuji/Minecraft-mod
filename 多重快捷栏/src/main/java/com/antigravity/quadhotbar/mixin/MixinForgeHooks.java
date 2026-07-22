package com.antigravity.quadhotbar.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {

    /**
     * 纯防崩静默拦截：强制返回 1 绕过 Forge 断言检查，不再输出调试日志。
     * 调试日志功能已独立收归至 [调试日志 DebugLogger] 模组中。
     */
    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"),
            require = 0
    )
    private static int bypassStackCountCheck1(ItemStack stack) {
        return 1;
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41613_()I"),
            require = 0
    )
    private static int bypassStackCountCheck2(ItemStack stack) {
        return 1;
    }
}
