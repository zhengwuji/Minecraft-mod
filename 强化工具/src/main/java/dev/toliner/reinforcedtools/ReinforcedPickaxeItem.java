package dev.toliner.reinforcedtools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;

public final class ReinforcedPickaxeItem extends PickaxeItem implements ReinforcedTool {
    private final ReinforcedToolMaterial material;

    public ReinforcedPickaxeItem(ReinforcedToolMaterial material, Item.Properties properties) {
        super(material, 1, -2.8F, properties);
        this.material = material;
    }

    @Override
    public ReinforcedToolMaterial reinforcedMaterial() {
        return material;
    }
}
