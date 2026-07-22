package com.antigravity.devhelper;

import com.antigravity.devhelper.network.DevHelperNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(DevHelper.MODID)
public class DevHelper {
    public static final String MODID = "devhelper";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public DevHelper() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            DevHelperNetwork.register();
            LOGGER.info("[开发者辅助] 网络数据包通道已成功注册并监听！");
        });
    }
}
