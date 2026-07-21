/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.item.BlockItem
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.level.block.Block
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.grim.workbenches;

import com.grim.workbenches.ModBlocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.ITEMS, (String)"grimpack");
    public static final RegistryObject<Item> IRON_WORKBENCH = ITEMS.register("iron_workbench", () -> new BlockItem((Block)ModBlocks.IRON_WORKBENCH.get(), new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_WORKBENCH = ITEMS.register("diamond_workbench", () -> new BlockItem((Block)ModBlocks.DIAMOND_WORKBENCH.get(), new Item.Properties()));
}
