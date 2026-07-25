package com.grim.workbenches;

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

public class WorkbenchBlock extends CraftingTableBlock {
    private final int multiplier;
    private final Component title;

    public WorkbenchBlock(BlockBehaviour.Properties properties, int multiplier, String titleKey) {
        super(properties);
        this.multiplier = multiplier;
        this.title = Component.translatable(titleKey);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        NetworkHooks.openScreen((ServerPlayer) player, state.getMenuProvider(level, pos), buf -> buf.writeInt(this.multiplier));
        player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
        return InteractionResult.CONSUME;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((containerId, playerInventory, player) -> new WorkbenchMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), this.multiplier), this.title);
    }
}