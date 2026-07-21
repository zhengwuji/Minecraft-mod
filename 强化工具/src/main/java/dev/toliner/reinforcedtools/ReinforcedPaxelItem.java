package dev.toliner.reinforcedtools;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ReinforcedPaxelItem extends DiggerItem implements ReinforcedTool {
    private final ReinforcedToolMaterial material;

    public ReinforcedPaxelItem(ReinforcedToolMaterial material, Properties properties) {
        super(4.0F, -2.4F, material, BlockTags.MINEABLE_WITH_PICKAXE, properties);
        this.material = material;
    }

    @Override
    public boolean isCorrectToolForDrops(BlockState state) {
        return true;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return this.speed;
    }

    @Override
    public ReinforcedToolMaterial reinforcedMaterial() {
        return material;
    }
}
