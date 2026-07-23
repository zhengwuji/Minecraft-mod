package com.antigravity.debuglogger;

import com.antigravity.debuglogger.util.LogCollector;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(DebugLogger.MOD_ID)
public class DebugLogger {
    public static final String MOD_ID = "debuglogger";
    public static final Logger LOGGER = LogManager.getLogger("DevDebugLogger");

    public DebugLogger() {
        // 🚀 在极早期构造阶段强制预热初始化 Lodestone 的 RenderHandler，彻底打断 Malum 渲染层空指针死锁！
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            try {
                Class<?> rhClass = Class.forName("team.lodestar.lodestone.handlers.RenderHandler");
                rhClass.getMethod("onClientSetup", net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent.class).invoke(null, (Object) null);
                LOGGER.info("[调试日志-HUNTER] 🛡️ 成功在 MOD 构造初期提前预热初始化 Lodestone RenderHandler！");
            } catch (Throwable t) {
                LOGGER.warn("[调试日志-HUNTER] 预热初始化 Lodestone RenderHandler 触发非致命警告:", t);
            }
        });

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(EventPriority.HIGHEST, this::onBuildCreativeTab);

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("[调试日志] Dev Debug Logger 模组（纯后台全自动防护引擎）初始化成功！");
    }

    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        try {
            for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : event.getEntries()) {
                ItemStack stack = entry.getKey();
                if (stack != null && !stack.isEmpty() && stack.getCount() != 1) {
                    LOGGER.warn("[调试日志-HUNTER] 🚨 成功防护拦截 Malum/非法物品: {} (原 count={}) -> 强制更正为 1",
                            stack.getItem(), stack.getCount());
                    stack.setCount(1);
                }
            }
        } catch (Throwable ignored) {
        }
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("[调试日志] 通用初始化就绪。全自动日志搜集与防护引擎启动。");
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("[调试日志] 客户端全自动保存模式已开启（纯后台全自动运行）。");
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
