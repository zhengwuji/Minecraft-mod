package com.grim.workbenches;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class WorkbenchMenu extends CraftingMenu {
    private final int multiplier;
    private final ContainerLevelAccess access;
    private final Player player;

    public WorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, int multiplier) {
        super(containerId, playerInventory, access);
        this.multiplier = multiplier;
        this.access = access;
        this.player = playerInventory.player;
    }

    @Override
    public MenuType<?> getType() {
        return ModMenus.WORKBENCH.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.access.evaluate((level, pos) -> {
            BlockState state = level.getBlockState(pos);
            return (state.is((Block) ModBlocks.IRON_WORKBENCH.get()) || state.is((Block) ModBlocks.DIAMOND_WORKBENCH.get())) 
                    && player.distanceToSqr((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) <= 64.0;
        }, true);
    }

    @Override
    public void slotsChanged(Container container) {
        this.access.execute((level, blockPos) -> {
            if (!level.isClientSide) {
                CraftingRecipe craftingrecipe;
                ServerPlayer serverplayer = (ServerPlayer) this.player;
                ItemStack itemstack = ItemStack.EMPTY;
                ResultContainer resultSlots = (ResultContainer) this.getSlot(0).container;
                CraftingContainer craftSlots = (CraftingContainer) this.getSlot(1).container;
                
                Optional<CraftingRecipe> optional = level.getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftSlots, level);
                if (optional.isPresent() && resultSlots.setRecipeUsed(level, serverplayer, (craftingrecipe = optional.get()))) {
                    ItemStack recipeOutput = craftingrecipe.assemble(craftSlots, level.registryAccess());
                    int maxStack = recipeOutput.getMaxStackSize();
                    int count = recipeOutput.getCount() * this.multiplier;
                    recipeOutput.setCount(maxStack == 1 ? count : Math.min(maxStack, count));
                    itemstack = recipeOutput;
                }
                resultSlots.setItem(0, itemstack);
                this.setRemoteSlot(0, itemstack);
                serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), 0, itemstack));
            }
        });
    }
}