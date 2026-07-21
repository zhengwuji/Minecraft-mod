package com.tfar.anviltweaks;

import com.tfar.anviltweaks.network.Message;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(AnvilTweaks.MODID)
public class AnvilTweaks {
    public static final String MODID = "anviltweaks";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);

    public static final BlockBehaviour.Properties ANVIL_PROPS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 1200.0F)
            .sound(SoundType.ANVIL);

    public static final RegistryObject<Block> ANVIL = BLOCKS.register("anvil", () -> new AnvilBlockv2(ANVIL_PROPS));
    public static final RegistryObject<Block> CHIPPED_ANVIL = BLOCKS.register("chipped_anvil", () -> new AnvilBlockv2(ANVIL_PROPS));
    public static final RegistryObject<Block> DAMAGED_ANVIL = BLOCKS.register("damaged_anvil", () -> new AnvilBlockv2(ANVIL_PROPS));

    public static final RegistryObject<Item> ANVIL_ITEM = ITEMS.register("anvil", () -> new BlockItem(ANVIL.get(), new Item.Properties()));
    public static final RegistryObject<Item> CHIPPED_ANVIL_ITEM = ITEMS.register("chipped_anvil", () -> new BlockItem(CHIPPED_ANVIL.get(), new Item.Properties()));
    public static final RegistryObject<Item> DAMAGED_ANVIL_ITEM = ITEMS.register("damaged_anvil", () -> new BlockItem(DAMAGED_ANVIL.get(), new Item.Properties()));

    public static final RegistryObject<BlockEntityType<AnvilTile>> ANVIL_TILE = BLOCK_ENTITY_TYPES.register("anvil_tile",
            () -> BlockEntityType.Builder.of(AnvilTile::new, ANVIL.get(), CHIPPED_ANVIL.get(), DAMAGED_ANVIL.get()).build(null));

    public static final RegistryObject<MenuType<RepairContainerv2>> ANVIL_CONTAINER_V2 = MENU_TYPES.register("anvil_container_v2",
            () -> IForgeMenuType.create((windowId, inv, data) -> new RepairContainerv2(windowId, inv, data.readBlockPos())));

    @SuppressWarnings("removal")
    public AnvilTweaks() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::setup);
        modEventBus.addListener(this::clientSetup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> Message.registerMessages(MODID));
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ANVIL_CONTAINER_V2.get(), AnvilScreenv2::new));
    }
}
