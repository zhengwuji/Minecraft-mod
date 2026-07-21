package dev.toliner.reinforcedtools;

public enum ReinforcedToolType {
    SWORD("sword", "Sword"),
    PICKAXE("pickaxe", "Pickaxe"),
    AXE("axe", "Axe"),
    SHOVEL("shovel", "Shovel"),
    HOE("hoe", "Hoe"),
    PAXEL("paxel", "Paxel"),
    BATTLE_AXE("battle_axe", "Battle Axe"),
    MINING_HAMMER("mining_hammer", "Mining Hammer");

    private final String id;
    private final String displayName;

    ReinforcedToolType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }
}
