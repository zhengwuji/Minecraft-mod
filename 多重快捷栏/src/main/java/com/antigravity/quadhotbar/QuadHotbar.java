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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@Mod(QuadHotbar.MOD_ID)
public class QuadHotbar {
    public static final String MOD_ID = "quadhotbar";
    public static final Logger LOGGER = LogManager.getLogger("QuadHotbarDebug");

    public QuadHotbar() {
        LOGGER.info("[QuadHotbar-DEBUG] 🚀【多重快捷栏】模组与崩溃诊断追踪日志系统初始化成功！");
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
     * 自动监控并记录 BuildCreativeModeTabContentsEvent 事件中的所有物品，捕获违规物品并打印详细日志！
     */
    private void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        try {
            String tabTitle = event.getTab() != null ? event.getTab().getDisplayName().getString() : "Unknown Tab";
            LOGGER.info("[QuadHotbar-DEBUG-EVENT] 正在处理标签页 [{}] 物品表...", tabTitle);

            for (Map.Entry<ItemStack, CreativeModeTab.TabVisibility> entry : event.getEntries()) {
                ItemStack stack = entry.getKey();
                if (stack != null && !stack.isEmpty()) {
                    int count = stack.getCount();
                    if (count != 1) {
                        String itemStr = stack.getItem().toString();
                        String modId = stack.getItem().getCreatorModId(stack);

                        LOGGER.error("================================================================================");
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 🚨 在事件中捕获到非法堆叠物品！");
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 标签页:   {}", tabTitle);
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 物品名称: {}", itemStr);
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 模组 ID:  {}", modId);
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 物品 Count: {} (应该为 1)", count);
                        LOGGER.error("[QuadHotbar-DEBUG-EVENT-HUNTER] 强制归一化 Count -> 1...");
                        LOGGER.error("================================================================================");

                        stack.setCount(1);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("[QuadHotbar-DEBUG-EVENT] 监控过程中出现异常:", e);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NetworkHandler::register);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
    }
}
