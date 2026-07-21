/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.stats.Stats
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.CraftingTableBlock
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraftforge.network.NetworkHooks
 */
package com.grim.workbenches;

import com.grim.workbenches.WorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class WorkbenchBlock
extends CraftingTableBlock {
    private final int multiplier;
    private final Component title;

    public WorkbenchBlock(BlockBehaviour.Properties properties, int multiplier, String titleKey) {
        super(properties);
        this.multiplier = multiplier;
        this.title = Component.m_237115_((String)titleKey);
    }

    public InteractionResult m_6227_(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.f_46443_) {
            return InteractionResult.SUCCESS;
        }
        NetworkHooks.openScreen((ServerPlayer)((ServerPlayer)player), (MenuProvider)state.m_60750_(level, pos), buf -> buf.writeInt(this.multiplier));
        player.m_36220_(Stats.f_12967_);
        return InteractionResult.CONSUME;
    }

    public MenuProvider m_7246_(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((containerId, playerInventory, player) -> new WorkbenchMenu(containerId, playerInventory, ContainerLevelAccess.m_39289_((Level)level, (BlockPos)pos), this.multiplier), this.title);
    }
}
