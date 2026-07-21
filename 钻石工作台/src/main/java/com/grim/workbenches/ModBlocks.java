/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.grim.workbenches;

import com.grim.workbenches.WorkbenchBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"grimpack");
    public static final RegistryObject<Block> IRON_WORKBENCH = BLOCKS.register("iron_workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.m_60926_((BlockBehaviour)Blocks.f_50091_), 2, "container.iron_workbench"));
    public static final RegistryObject<Block> DIAMOND_WORKBENCH = BLOCKS.register("diamond_workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.m_60926_((BlockBehaviour)Blocks.f_50091_), 4, "container.diamond_workbench"));
}
