package com.customemc.mixin;

import moze_intel.projecte.api.ItemInfo;
import moze_intel.projecte.emc.nbt.NBTManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = NBTManager.class, remap = false)
public class MixinProjectENBTManager {
    // 恢复 ProjectE 默认安全 NBT 处理，彻底消除转换桌紫黑材质块错误
}
