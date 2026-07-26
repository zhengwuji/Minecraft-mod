package com.antigravity.debuglogger.mixin;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinProjectEDebugLogger {
    // 纯静默诊断支持，不再强行拦截与重置 searchpage，彻底解决输入关键词后无法翻页与主线程卡死问题
}
