package com.antigravity.foodbuffbag;

import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.client.FoodBuffScreen;
import com.antigravity.foodbuffbag.inventory.FoodBuffMenu;
import com.antigravity.foodbuffbag.network.NetworkHandler;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(FoodBuffBag.MOD_ID)
public class FoodBuffBag {
    public static final String MOD_ID = "foodbuffbag";

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MOD_ID);

    public static final RegistryObject<MenuType<FoodBuffMenu>> FOOD_BUFF_MENU = MENU_TYPES.register("food_buff_menu",
            () -> IForgeMenuType.create(FoodBuffMenu::new));

    @SuppressWarnings("removal")
    public FoodBuffBag() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MENU_TYPES.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(this::registerCapabilities);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(FOOD_BUFF_MENU.get(), FoodBuffScreen::new);
        });
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.register(FoodBuffProvider.class);
    }
}
