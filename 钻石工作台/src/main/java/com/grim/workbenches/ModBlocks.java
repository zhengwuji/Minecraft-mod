package com.grim.workbenches;

import com.grim.workbenches.ModItems;
import com.grim.workbenches.WorkbenchBlock;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "grimpack");
    public static final RegistryObject<Block> IRON_WORKBENCH = BLOCKS.register("iron_workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE), 2, "container.iron_workbench"));
    public static final RegistryObject<Block> DIAMOND_WORKBENCH = BLOCKS.register("diamond_workbench", () -> new WorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.CRAFTING_TABLE), 4, "container.diamond_workbench"));
}
