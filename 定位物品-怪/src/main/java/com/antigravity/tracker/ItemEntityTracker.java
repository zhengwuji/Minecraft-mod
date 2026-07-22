package com.antigravity.tracker;

import com.antigravity.tracker.client.KeyInputHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ItemEntityTracker.MODID)
public class ItemEntityTracker {
    public static final String MODID = "itementitytracker";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ItemEntityTracker() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        LOGGER.info("[定位物品-怪] 模组已初始化完成！");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyInputHandler::registerKeyMappings);
        });
        LOGGER.info("[定位物品-怪] 客户端渲染与 F6 快捷键注册完成！");
    }
}
