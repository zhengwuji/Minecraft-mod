package com.antigravity.quadhotbar;

import com.antigravity.quadhotbar.client.HotbarHudOverlay;
import com.antigravity.quadhotbar.client.KeyInputHandler;
import com.antigravity.quadhotbar.network.NetworkHandler;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Map;

@Mod(QuadHotbar.MOD_ID)
public class QuadHotbar {
    public static final String MOD_ID = "quadhotbar";

    public QuadHotbar() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::onBuildCreativeTab);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::clientSetup);
            modEventBus.addListener(KeyInputHandler::registerKeyMappings);
            MinecraftForge.EVENT_BUS.register(HotbarHudOverlay.class);
        }
    }

    /**
     * 自动拦截并修正任何第三方模组 (如混沌降生 born_in_chaos) 在创造模式标签页注册 count > 1 的物品。
     * 完美修复创造模式下按 E 崩溃 (IllegalArgumentException: The stack count must be 1) 的重大漏洞。
     */
    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : event.getEntries()) {
            ItemStack stack = entry.getKey();
            if (stack != null && !stack.isEmpty() && stack.getCount() != 1) {
                stack.setCount(1);
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
