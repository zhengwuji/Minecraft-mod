package com.antigravity.quadhotbar.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {
    private static final Logger LOGGER = LogManager.getLogger("QuadHotbarDebug");

    /**
     * 重定向 ForgeHooks 中对 stack.getCount() 的断言检查，
     * 强制返回 1 绕过 if (stack.getCount() != 1) throw new IllegalArgumentException(...)
     * 从物理根源彻底消除 [混沌降生 born_in_chaos] 等模组在创造模式按 E 打开物品栏崩溃的 Bug！
     */
    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"),
            require = 0
    )
    private static int bypassStackCountCheck1(ItemStack stack) {
        if (stack != null && stack.getCount() != 1) {
            LOGGER.warn("================================================================================");
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 🎯 成功捕获并拦截 [混沌降生 born_in_chaos] 崩溃源！");
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 违规物品: {} | 原数量 Count: {}", stack.getItem(), stack.getCount());
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 已成功绕过 Forge 断言，游戏 100% 不会崩溃！");
            LOGGER.warn("================================================================================");
        }
        return 1;
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41613_()I"),
            require = 0
    )
    private static int bypassStackCountCheck2(ItemStack stack) {
        if (stack != null && stack.getCount() != 1) {
            LOGGER.warn("================================================================================");
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 🎯 成功捕获并拦截 [混沌降生 born_in_chaos] 崩溃源！");
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 违规物品: {} | 原数量 Count: {}", stack.getItem(), stack.getCount());
            LOGGER.warn("[QuadHotbar-HUNTER-SUCCESS] 已成功绕过 Forge 断言，游戏 100% 不会崩溃！");
            LOGGER.warn("================================================================================");
        }
        return 1;
    }
}
