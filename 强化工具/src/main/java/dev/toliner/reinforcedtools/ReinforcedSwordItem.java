package dev.toliner.reinforcedtools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;

public final class ReinforcedSwordItem extends SwordItem implements ReinforcedTool {
    private final ReinforcedToolMaterial material;

    public ReinforcedSwordItem(ReinforcedToolMaterial material, Item.Properties properties) {
        super(material, 3, -2.4F, properties);
        this.material = material;
    }

    @Override
    public ReinforcedToolMaterial reinforcedMaterial() {
        return material;
    }
}
