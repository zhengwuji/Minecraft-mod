package com.antigravity.quadhotbar.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = CreativeModeTab.class, priority = 900)
public abstract class MixinCreativeModeTab {
    private static final Logger LOGGER = LogManager.getLogger("QuadHotbarDebug");

    /**
     * 拦截 CreativeModeTab.buildContents() 内部每次对 Output.accept 的调用
     * 输出完整调试日志，并捕获任何 count != 1 的非法 Stack！
     */
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
                                           CreativeModeTab.TabVisibility visibility) {
        if (stack != null && !stack.isEmpty()) {
            int count = stack.getCount();
            String itemStr = stack.getItem().toString();
            String modId = stack.getItem().getCreatorModId(stack);

            if (count != 1) {
                LOGGER.error("================================================================================");
                LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 🚨 发现导致崩溃的非法物品！");
                LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 物品名称: {}", itemStr);
                LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 模组 ID:  {}", modId);
                LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 物品数量 Count: {} (必须为 1 !)", count);
                LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 正在强制将其 Count 修改为 1 以防止创造模式按 E 崩溃...");
                LOGGER.error("================================================================================");

                // 修正为 1
                stack.setCount(1);
            } else {
                LOGGER.debug("[QuadHotbar-DEBUG] 正常注册物品: {} (Mod: {})", itemStr, modId);
            }
        }
        output.accept(stack, visibility);
    }
}
