package com.antigravity.foodbuffbag.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FoodBuffProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<ItemStackHandler> FOOD_BUFF_CAP = CapabilityManager.get(new CapabilityToken<>() {});

    // 100 页 x 54 槽 = 5400 槽位
    public static final int TOTAL_SLOTS = 5400;
    public static final int SLOTS_PER_PAGE = 54;
    public static final int MAX_PAGES = 100;

    private final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            // 只允许食物或带药水/食谱特性的物品放入
            return stack.isEdible() || stack.getItem().isEdible();
        }
    };

    private final LazyOptional<ItemStackHandler> optional = LazyOptional.of(() -> inventory);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == FOOD_BUFF_CAP) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return inventory.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        inventory.deserializeNBT(nbt);
    }
}
