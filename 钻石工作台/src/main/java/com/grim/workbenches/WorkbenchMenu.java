/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.Container
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.inventory.CraftingContainer
 *  net.minecraft.world.inventory.CraftingMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.ResultContainer
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.crafting.CraftingRecipe
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 */
package com.grim.workbenches;

import com.grim.workbenches.ModBlocks;
import com.grim.workbenches.ModMenus;
import java.util.Optional;
import net.minecraft.network.protocol.Packet;
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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WorkbenchMenu
extends CraftingMenu {
    private final int multiplier;
    private final ContainerLevelAccess access;
    private final Player player;

    public WorkbenchMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, int multiplier) {
        super(containerId, playerInventory, access);
        this.multiplier = multiplier;
        this.access = access;
        this.player = playerInventory.f_35978_;
    }

    public MenuType<?> m_6772_() {
        return (MenuType)ModMenus.WORKBENCH.get();
    }

    public boolean m_6875_(Player player) {
        return (Boolean)this.access.m_39299_((level, pos) -> {
            BlockState state = level.m_8055_(pos);
            return (state.m_60713_((Block)ModBlocks.IRON_WORKBENCH.get()) || state.m_60713_((Block)ModBlocks.DIAMOND_WORKBENCH.get())) && player.m_20275_((double)pos.m_123341_() + 0.5, (double)pos.m_123342_() + 0.5, (double)pos.m_123343_() + 0.5) <= 64.0;
        }, (Object)true);
    }

    public void m_6199_(Container container) {
        this.access.m_39292_((level, blockPos) -> {
            if (!level.f_46443_) {
                CraftingRecipe craftingrecipe;
                ServerPlayer serverplayer = (ServerPlayer)this.player;
                ItemStack itemstack = ItemStack.f_41583_;
                ResultContainer resultSlots = (ResultContainer)this.m_38853_((int)0).f_40218_;
                CraftingContainer craftSlots = (CraftingContainer)this.m_38853_((int)1).f_40218_;
                Optional optional = level.m_7654_().m_129894_().m_44015_(RecipeType.f_44107_, (Container)craftSlots, level);
                if (optional.isPresent() && resultSlots.m_40135_(level, serverplayer, (Recipe)(craftingrecipe = (CraftingRecipe)optional.get()))) {
                    ItemStack recipeOutput = craftingrecipe.m_5874_((Container)craftSlots, level.m_9598_());
                    int maxStack = recipeOutput.m_41741_();
                    int count = recipeOutput.m_41613_() * this.multiplier;
                    recipeOutput.m_41764_(maxStack == 1 ? count : Math.min(maxStack, count));
                    itemstack = recipeOutput;
                }
                resultSlots.m_6836_(0, itemstack);
                this.m_150404_(0, itemstack);
                serverplayer.f_8906_.m_9829_((Packet)new ClientboundContainerSetSlotPacket(this.f_38840_, this.m_182425_(), 0, itemstack));
            }
        });
    }
}
