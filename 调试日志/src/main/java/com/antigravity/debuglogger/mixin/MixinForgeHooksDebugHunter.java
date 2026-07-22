package com.antigravity.debuglogger.mixin;

import com.antigravity.debuglogger.DebugLogger;
import com.antigravity.debuglogger.util.LogCollector;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooksDebugHunter {

    private static int getStackCount(ItemStack stack) {
        if (stack == null) return 0;
        try {
            return stack.getCount();
        } catch (Throwable e) {
            try {
                java.lang.reflect.Method m = ItemStack.class.getMethod("m_41613_");
                return (Integer) m.invoke(stack);
            } catch (Throwable t) {
                return 1;
            }
        }
    }

    private static String getSafeItemName(ItemStack stack) {
        if (stack == null) return "null";
        try {
            return stack.getItem().toString();
        } catch (Throwable t1) {
            try {
                java.lang.reflect.Method m = ItemStack.class.getMethod("m_41720_");
                Object item = m.invoke(stack);
                return item != null ? item.toString() : "unknown";
            } catch (Throwable t2) {
                return "unknown";
            }
        }
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"),
            require = 0
    )
    private static int bypassStackCountCheck1(ItemStack stack) {
        try {
            int count = getStackCount(stack);
            if (stack != null && count != 1) {
                String detail = "捕获创造模式物品栏违规 Stack 检查: 物品=" + getSafeItemName(stack) + " | 数量=" + count;
                LogCollector.recordInterceptedAssertion(detail);
                DebugLogger.LOGGER.warn("[调试日志-HUNTER] 🎯 " + detail);
            }
        } catch (Throwable ignored) {}
        return 1;
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41613_()I"),
            require = 0
    )
    private static int bypassStackCountCheck2(ItemStack stack) {
        try {
            int count = getStackCount(stack);
            if (stack != null && count != 1) {
                String detail = "捕获创造模式物品栏违规 Stack 检查: 物品=" + getSafeItemName(stack) + " | 数量=" + count;
                LogCollector.recordInterceptedAssertion(detail);
                DebugLogger.LOGGER.warn("[调试日志-HUNTER] 🎯 " + detail);
            }
        } catch (Throwable ignored) {}
        return 1;
    }
}
