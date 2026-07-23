package com.antigravity.debuglogger.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CreativeModeTab.class, priority = 900)
public abstract class MixinCreativeModeTab {
    private static final Logger LOGGER = LogManager.getLogger("DebugLogger");

    @Redirect(
            method = "buildContents(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V"
            ),
            require = 0
    )
    private void fixStackCountBeforeAccept(CreativeModeTab.Output output,
                                           ItemStack stack,
                                           Object visibility) {
        if (stack != null && !stack.isEmpty()) {
            int count = stack.getCount();
            if (count != 1) {
                String itemStr = stack.getItem().toString();
                String modId = stack.getItem().getCreatorModId(stack);

                LOGGER.error("================================================================================");
                LOGGER.error("[DebugLogger-HUNTER] 🚨 在创造页签与 JEI 构建中捕获到非法堆叠物品！");
                LOGGER.error("[DebugLogger-HUNTER] 物品名称: {}", itemStr);
                LOGGER.error("[DebugLogger-HUNTER] 模组 ID:  {}", modId);
                LOGGER.error("[DebugLogger-HUNTER] 物品数量 Count: {} (应该为 1)", count);
                LOGGER.error("[DebugLogger-HUNTER] 正在强制将其 Count 修正为 1 以防止创造模式/JEI崩溃卡死...");
                LOGGER.error("================================================================================");

                stack.setCount(1);
            }
        }
        output.accept(stack, (CreativeModeTab.TabVisibility) visibility);
    }
}
