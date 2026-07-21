package dev.toliner.reinforcedtools;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;

public enum ReinforcedToolMaterial implements Tier {
    WOOD("wood", "Wood", "wooden", 140, 3.0F, 0.0F, 18, "oak_planks", 6.0F, -3.2F, 0.0F, -3.0F, 0),
    STONE("stone", "Stone", "stone", 240, 4.5F, 1.0F, 8, "cobblestone", 7.0F, -3.2F, -1.0F, -2.0F, 1),
    COPPER("copper", "Copper", "copper", 320, 5.5F, 1.0F, 15, "copper_ingot", 7.0F, -3.2F, -1.0F, -2.0F, 1),
    IRON("iron", "Iron", "iron", 700, 7.0F, 2.0F, 16, "iron_ingot", 6.0F, -3.1F, -2.0F, -1.0F, 2),
    DIAMOND("diamond", "Diamond", "diamond", 2500, 9.0F, 3.0F, 13, "diamond", 5.0F, -3.0F, -3.0F, 0.0F, 3),
    NETHERITE("netherite", "Netherite", "netherite", 4000, 10.0F, 5.0F, 18, "netherite_ingot", 5.0F, -3.0F, -4.0F, 0.0F, 4),
    GOLD("gold", "Gold", "golden", 100, 14.0F, 0.0F, 25, "gold_ingot", 6.0F, -3.0F, 0.0F, -3.0F, 0);

    private final String id;
    private final String displayName;
    private final String vanillaPrefix;
    private final int durability;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final String modelItem;
    private final float axeAttackDamageBaseline;
    private final float axeAttackSpeedBaseline;
    private final float hoeAttackDamageBaseline;
    private final float hoeAttackSpeedBaseline;
    private final int level;

    ReinforcedToolMaterial(
        String id,
        String displayName,
        String vanillaPrefix,
        int durability,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        String modelItem,
        float axeAttackDamageBaseline,
        float axeAttackSpeedBaseline,
        float hoeAttackDamageBaseline,
        float hoeAttackSpeedBaseline,
        int level
    ) {
        this.id = id;
        this.displayName = displayName;
        this.vanillaPrefix = vanillaPrefix;
        this.durability = durability;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.modelItem = modelItem;
        this.axeAttackDamageBaseline = axeAttackDamageBaseline;
        this.axeAttackSpeedBaseline = axeAttackSpeedBaseline;
        this.hoeAttackDamageBaseline = hoeAttackDamageBaseline;
        this.hoeAttackSpeedBaseline = hoeAttackSpeedBaseline;
        this.level = level;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public String vanillaPrefix() {
        return vanillaPrefix;
    }

    public String modelItem() {
        return modelItem;
    }

    @Override
    public int getUses() {
        return durability;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return switch (this) {
            case WOOD -> Ingredient.of(ItemTags.PLANKS);
            case STONE -> Ingredient.of(ItemTags.STONE_TOOL_MATERIALS);
            case COPPER -> Ingredient.of(Items.COPPER_INGOT);
            case IRON -> Ingredient.of(Items.IRON_INGOT);
            case GOLD -> Ingredient.of(Items.GOLD_INGOT);
            case DIAMOND -> Ingredient.of(Items.DIAMOND);
            case NETHERITE -> Ingredient.of(Items.NETHERITE_INGOT);
        };
    }

    public float axeAttackDamageBaseline() {
        return axeAttackDamageBaseline;
    }

    public float axeAttackSpeedBaseline() {
        return axeAttackSpeedBaseline;
    }

    public float hoeAttackDamageBaseline() {
        return hoeAttackDamageBaseline;
    }

    public float hoeAttackSpeedBaseline() {
        return hoeAttackSpeedBaseline;
    }
}
