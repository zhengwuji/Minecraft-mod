package dev.toliner.reinforcedtools;

import net.minecraft.world.item.AxeItem;

public class ReinforcedBattleAxeItem extends AxeItem implements ReinforcedTool {
    private final ReinforcedToolMaterial material;

    public ReinforcedBattleAxeItem(ReinforcedToolMaterial material, Properties properties) {
        super(material, 8.0F, -2.4F, properties.durability(material.getUses() * 3));
        this.material = material;
    }

    @Override
    public ReinforcedToolMaterial reinforcedMaterial() {
        return material;
    }
}
