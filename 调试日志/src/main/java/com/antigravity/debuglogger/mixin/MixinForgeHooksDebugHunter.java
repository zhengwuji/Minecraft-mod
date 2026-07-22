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
        } catch (NoSuchMethodError e) {
            try {
                java.lang.reflect.Method m = ItemStack.class.getMethod("m_41613_");
                return (Integer) m.invoke(stack);
            } catch (Throwable t) {
                return 1;
            }
        }
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getCount()I"),
            require = 0
    )
    private static int bypassStackCountCheck1(ItemStack stack) {
        int count = getStackCount(stack);
        if (stack != null && count != 1) {
            String detail = "捕获创造模式物品栏违规 Stack 检查: 物品=" + stack.getItem() + " | 数量=" + count;
            LogCollector.recordInterceptedAssertion(detail);
            DebugLogger.LOGGER.warn("[调试日志-HUNTER] 🎯 " + detail);
        }
        return 1;
    }

    @Redirect(
            method = "*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;m_41613_()I"),
            require = 0
    )
    private static int bypassStackCountCheck2(ItemStack stack) {
        int count = getStackCount(stack);
        if (stack != null && count != 1) {
            String detail = "捕获创造模式物品栏违规 Stack 检查: 物品=" + stack.getItem() + " | 数量=" + count;
            LogCollector.recordInterceptedAssertion(detail);
            DebugLogger.LOGGER.warn("[调试日志-HUNTER] 🎯 " + detail);
        }
        return 1;
    }
}
