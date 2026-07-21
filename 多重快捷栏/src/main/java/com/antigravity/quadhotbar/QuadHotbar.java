package com.antigravity.quadhotbar;

import com.antigravity.quadhotbar.client.HotbarHudOverlay;
import com.antigravity.quadhotbar.client.KeyInputHandler;
import com.antigravity.quadhotbar.network.NetworkHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(QuadHotbar.MOD_ID)
public class QuadHotbar {
    public static final String MOD_ID = "quadhotbar";

    public QuadHotbar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyInputHandler::registerKeyMappings);
            MinecraftForge.EVENT_BUS.register(HotbarHudOverlay.class);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
