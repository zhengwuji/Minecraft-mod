package com.customemc.mixin;

import moze_intel.projecte.gameObjs.container.inventory.TransmutationInventory;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TransmutationInventory.class, remap = false)
public class MixinTransmutationInventory {
}
