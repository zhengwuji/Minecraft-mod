package com.antigravity.quadhotbar.mixin;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {
    private static final Logger LOGGER = LogManager.getLogger("QuadHotbarDebug");

    /**
     * 修正正确的 Forge 1.20.1 方法签名：
     * (CreativeModeTab, ResourceKey, DisplayItemsGenerator, ItemDisplayParameters, Output)
     */
    @Inject(method = "onCreativeModeTabBuildContents", at = @At("HEAD"))
    private static void onStartBuildTabContents(CreativeModeTab tab,
                                                ResourceKey<CreativeModeTab> tabKey,
                                                CreativeModeTab.DisplayItemsGenerator generator,
                                                CreativeModeTab.ItemDisplayParameters parameters,
                                                CreativeModeTab.Output output,
                                                CallbackInfo ci) {
        try {
            String title = tab != null && tab.getDisplayName() != null ? tab.getDisplayName().getString() : "Unknown";
            LOGGER.info("[QuadHotbar-DEBUG] >>> 开始构建创造模式标签页: [{}] (Key: {})", title, tabKey != null ? tabKey.location() : "null");
        } catch (Exception ignored) {}
    }

    /**
     * 对传入 ForgeHooks.onCreativeModeTabBuildContents 的 Output 进行安全代理包装。
     * 无论任何模组在事件、静态构建或各种回调中通过 output.accept 添加物品，
     * 都拦截其 Stack Count，如果 != 1 则详细输出红字日志并强制归一为 1，防止崩溃！
     */
    @ModifyVariable(method = "onCreativeModeTabBuildContents", at = @At("HEAD"), argsOnly = true)
    private static CreativeModeTab.Output wrapOutputWithCrashProtection(CreativeModeTab.Output originalOutput,
                                                                         CreativeModeTab tab,
                                                                         ResourceKey<CreativeModeTab> tabKey) {
        if (originalOutput == null) return null;

        return (stack, visibility) -> {
            if (stack != null && !stack.isEmpty()) {
                int count = stack.getCount();
                if (count != 1) {
                    String itemStr = stack.getItem().toString();
                    String modId = stack.getItem().getCreatorModId(stack);
                    String tabTitle = tab != null && tab.getDisplayName() != null ? tab.getDisplayName().getString() : "Unknown";

                    LOGGER.error("================================================================================");
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 🚨 捕获到导致崩溃的违法堆叠物品！");
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 标签页:   [{}] (Key: {})", tabTitle, tabKey != null ? tabKey.location() : "null");
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 物品名称: {}", itemStr);
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 模组 ID:  {}", modId);
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 物品 Count: {} ( Forge 强制断言必须为 1 !)", count);
                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 详细调用栈追踪如下:");
                    
                    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                    for (int i = 2; i < Math.min(12, stackTrace.length); i++) {
                        LOGGER.error("    at {}", stackTrace[i]);
                    }

                    LOGGER.error("[QuadHotbar-DEBUG-CRASH-HUNTER] 正在强制将其 Count 修改为 1 以完美防止创造模式按 E 崩溃...");
                    LOGGER.error("================================================================================");

                    // 强制修复
                    stack.setCount(1);
                }
            }
            originalOutput.accept(stack, visibility);
        };
    }
}
