package com.antigravity.debuglogger;

import com.antigravity.debuglogger.client.KeyInputHandler;
import com.antigravity.debuglogger.util.LogCollector;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(DebugLogger.MOD_ID)
public class DebugLogger {
    public static final String MOD_ID = "debuglogger";
    public static final Logger LOGGER = LogManager.getLogger("DevDebugLogger");

    public DebugLogger() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[调试日志] Dev Debug Logger 模组已初始化！");
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[调试日志] 通用初始化就绪。全自动日志搜集与防护引擎启动。");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            modEventBusAddListener();
        });
    }

    private void modEventBusAddListener() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(KeyInputHandler::registerKeyMappings);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        LOGGER.info("[调试日志] 检测到服务端停止，正在全自动生成调试报告...");
        LogCollector.exportDevReport(null, "服务端/世界退出时自动保存");
    }

    @SubscribeEvent
    public void onClientLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        LOGGER.info("[调试日志] 检测到客户端退出世界，正在全自动生成调试报告...");
        LogCollector.exportDevReport(null, "退出世界时自动保存");
    }
}
