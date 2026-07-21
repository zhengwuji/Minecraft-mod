package com.antigravity.quadhotbar.mixin;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ForgeHooks.class, remap = false)
public class MixinForgeHooks {
    private static final Logger LOGGER = LogManager.getLogger("QuadHotbarDebug");

    /**
     * 捕获 onCreativeModeTabBuildContents 流程
     */
    @Inject(method = "onCreativeModeTabBuildContents", at = @At("HEAD"))
    private static void onStartBuildTabContents(CreativeModeTab tab, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output, CallbackInfo ci) {
        try {
            LOGGER.info("[QuadHotbar-DEBUG] >>> 开始构建创造模式标签页: {}", tab.getDisplayName().getString());
        } catch (Exception ignored) {}
    }
}
