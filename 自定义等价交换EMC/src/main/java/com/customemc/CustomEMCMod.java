package com.customemc;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(CustomEMCMod.MODID)
public class CustomEMCMod {
    public static final String MODID = "custom_emc";
    public static final Logger LOGGER = LogManager.getLogger();

    public CustomEMCMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {
        ConfigManager.loadConfig();
        LOGGER.info("[自定义等价交换EMC] 模组初始化完成！");
    }
}
