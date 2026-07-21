package com.antigravity.quadhotbar.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 拦截 CreativeModeTab$Output 的 accept 方法内部——
 * Forge 在 lambda$onCreativeModeTabBuildContents$17 里做了 assert count==1，
 * 我们在 lambda 调用 Output#accept 前把 count 强制设为 1，
 * 这样即使 born_in_chaos 等模组传入 count>1 的 stack 也不会崩溃。
 *
 * 注意：这里 Mixin 的目标是 CreativeModeTab（外部类），
 * 拦截的是其内部 buildContents 流程里调用 Output.accept 的位置。
 */
@Mixin(value = CreativeModeTab.class, priority = 900)
public abstract class MixinCreativeModeTab {

    /**
     * 拦截 CreativeModeTab.buildContents() 内部每次对 Output.accept(ItemStack, TabVisibility) 的调用，
     * 在真正传入之前把 stack.count 强制规范为 1，防止 born_in_chaos 等模组崩溃。
     */
    @Redirect(
            method = "buildContents(Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/CreativeModeTab$Output;accept(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/CreativeModeTab$TabVisibility;)V"
            ),
            require = 0  // 如果目标不存在也不报错（兼容性保护）
    )
    private void fixStackCountBeforeAccept(CreativeModeTab.Output output,
                                           ItemStack stack,
                                           CreativeModeTab.TabVisibility visibility) {
        if (stack != null && !stack.isEmpty() && stack.getCount() != 1) {
            stack.setCount(1);
        }
        output.accept(stack, visibility);
    }
}
