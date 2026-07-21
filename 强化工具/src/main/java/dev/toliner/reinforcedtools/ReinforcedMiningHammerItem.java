package dev.toliner.reinforcedtools;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedMiningHammerItem extends PickaxeItem implements ReinforcedTool {
    private final ReinforcedToolMaterial material;

    public ReinforcedMiningHammerItem(ReinforcedToolMaterial material, Properties properties) {
        super(material, 1, -2.8F, properties.durability(material.getUses() * 6));
        this.material = material;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        if (state.is(BlockTags.MINEABLE_WITH_PICKAXE) || state.is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            return this.speed * 1.8F;
        }
        return super.getDestroySpeed(stack, state);
    }

    @Override
    public ReinforcedToolMaterial reinforcedMaterial() {
        return material;
    }
}
