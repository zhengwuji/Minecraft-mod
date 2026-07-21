/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.client.gui.screens.MenuScreens
 *  net.minecraft.client.gui.screens.inventory.CraftingScreen
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.item.CreativeModeTabs
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.common.MinecraftForge
 *  net.minecraftforge.event.BuildCreativeModeTabContentsEvent
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
 *  org.slf4j.Logger
 */
package com.grim.workbenches;

import com.grim.workbenches.ModBlocks;
import com.grim.workbenches.ModItems;
import com.grim.workbenches.ModMenus;
import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(value="grimpack")
public class GrimWorkbenches {
    public static final String MODID = "grimpack";
    private static final Logger LOGGER = LogUtils.getLogger();

    public GrimWorkbenches(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register((Object)this);
        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Grim Workbenches Common Setup");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.f_256791_) {
            event.m_246326_((ItemLike)ModItems.IRON_WORKBENCH.get());
            event.m_246326_((ItemLike)ModItems.DIAMOND_WORKBENCH.get());
        }
    }

    @Mod.EventBusSubscriber(modid="grimpack", bus=Mod.EventBusSubscriber.Bus.MOD, value={Dist.CLIENT})
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.m_96206_((MenuType)((MenuType)ModMenus.WORKBENCH.get()), CraftingScreen::new));
        }
    }
}
