package com.tfar.anviltweaks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class RepairContainerv2 extends AnvilMenu {

    private final BlockPos pos;
    private final AnvilTile tile;

    public RepairContainerv2(int id, Inventory playerInventory, BlockPos pos) {
        this(id, playerInventory, playerInventory.player, pos);
    }

    public RepairContainerv2(int id, Inventory playerInventory, Player player, BlockPos pos) {
        super(id, playerInventory, ContainerLevelAccess.create(player.level(), pos));
        this.pos = pos;
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        if (blockEntity instanceof AnvilTile tileEntity) {
            this.tile = tileEntity;
            // Bind input slots to tile entity handler if valid
            if (this.slots.size() >= 2) {
                this.inputSlots.setItem(0, this.tile.handler.getStackInSlot(0));
                this.inputSlots.setItem(1, this.tile.handler.getStackInSlot(1));
            }
        } else {
            this.tile = null;
        }
    }

    @Override
    public void createResult() {
        super.createResult();
        saveTileState();
    }

    @Override
    public void removed(Player player) {
        saveTileState();
        // Clear input slots in container so AnvilMenu doesn't drop items on the ground
        this.inputSlots.setItem(0, ItemStack.EMPTY);
        this.inputSlots.setItem(1, ItemStack.EMPTY);
        super.removed(player);
    }

    public void saveTileState() {
        if (this.tile != null && !this.tile.getLevel().isClientSide) {
            this.tile.handler.setStackInSlot(0, this.inputSlots.getItem(0));
            this.tile.handler.setStackInSlot(1, this.inputSlots.getItem(1));
            this.tile.setChanged();
        }
    }
}
