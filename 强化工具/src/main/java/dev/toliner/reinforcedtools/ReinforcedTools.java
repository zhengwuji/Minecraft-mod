package dev.toliner.reinforcedtools;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

@Mod(ReinforcedTools.MOD_ID)
public final class ReinforcedTools {
    public static final String MOD_ID = "reinforced_tools";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MOD_ID);

    public static final Map<ReinforcedToolMaterial, Map<ReinforcedToolType, RegistryObject<Item>>> TOOLS = new EnumMap<>(ReinforcedToolMaterial.class);
    public static final Map<ReinforcedToolMaterial, RegistryObject<Item>> TOOL_RODS = new EnumMap<>(ReinforcedToolMaterial.class);
    public static final Map<ReinforcedToolMaterial, RegistryObject<RepairKitItem>> REPAIR_KITS = new EnumMap<>(ReinforcedToolMaterial.class);

    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tools", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.reinforced_tools.tools"))
            .icon(() -> new ItemStack(TOOLS.get(ReinforcedToolMaterial.DIAMOND).get(ReinforcedToolType.PICKAXE).get()))
            .displayItems((parameters, output) -> {
                for (var material : ReinforcedToolMaterial.values()) {
                    for (var type : ReinforcedToolType.values()) {
                        output.accept(TOOLS.get(material).get(type).get());
                    }
                    output.accept(TOOL_RODS.get(material).get());
                    output.accept(REPAIR_KITS.get(material).get());
                }
            })
            .build());

    public ReinforcedTools() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        for (var material : ReinforcedToolMaterial.values()) {
            var materialTools = new EnumMap<ReinforcedToolType, RegistryObject<Item>>(ReinforcedToolType.class);
            for (var type : ReinforcedToolType.values()) {
                var registration = ITEMS.register(toolId(material, type), () -> createTool(material, type, new Item.Properties()));
                materialTools.put(type, registration);
            }
            TOOLS.put(material, materialTools);

            var toolRod = ITEMS.register(
                    toolRodId(material),
                    () -> new Item(new Item.Properties())
            );
            TOOL_RODS.put(material, toolRod);

            var repairKit = ITEMS.register(
                    repairKitId(material),
                    () -> new RepairKitItem(material, new Item.Properties().stacksTo(16))
            );
            REPAIR_KITS.put(material, repairKit);
        }

        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }

    private static Item createTool(ReinforcedToolMaterial material, ReinforcedToolType type, Item.Properties properties) {
        if (material == ReinforcedToolMaterial.NETHERITE) {
            properties.fireResistant();
        }

        return switch (type) {
            case SWORD -> new ReinforcedSwordItem(material, properties);
            case PICKAXE -> new ReinforcedPickaxeItem(material, properties);
            case AXE -> new ReinforcedAxeItem(material, properties);
            case SHOVEL -> new ReinforcedShovelItem(material, properties);
            case HOE -> new ReinforcedHoeItem(material, properties);
            case PAXEL -> new ReinforcedPaxelItem(material, properties);
            case BATTLE_AXE -> new ReinforcedBattleAxeItem(material, properties);
            case MINING_HAMMER -> new ReinforcedMiningHammerItem(material, properties);
        };
    }

    public static String toolId(ReinforcedToolMaterial material, ReinforcedToolType type) {
        return "reinforced_" + material.id() + "_" + type.id();
    }

    public static String toolRodId(ReinforcedToolMaterial material) {
        return "reinforced_" + material.id() + "_tool_rod";
    }

    public static String repairKitId(ReinforcedToolMaterial material) {
        return "reinforced_" + material.id() + "_repair_kit";
    }
}
