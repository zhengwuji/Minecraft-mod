package com.antigravity.debuglogger.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "debuglogger", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CuriosDebugTracker {
    private static final Logger LOGGER = LogManager.getLogger("CuriosDebugTracker");

    @SubscribeEvent
    public static void onCommandExecuted(CommandEvent event) {
        String parseCommand = event.getParseResults().getReader().getString();
        if (parseCommand != null && parseCommand.startsWith("/curios")) {
            LOGGER.info("[CURIOS-COMMAND-DIAGNOSTIC] 检测到玩家输入命令: '{}'", parseCommand);

            try {
                if (event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player) {
                    LOGGER.info("[CURIOS-DIAGNOSTIC] 目标玩家: {}", player.getScoreboardName());

                    // 1. 抓取 Curios 官方的能力接口
                    Optional<ICuriosItemHandler> curiosHandler = CuriosApi.getCuriosInventory(player).resolve();
                    if (curiosHandler.isPresent()) {
                        Map<String, ICurioStacksHandler> stacksHandlerMap = curiosHandler.get().getCurios();
                        LOGGER.info("[CURIOS-DIAGNOSTIC] 当前玩家拥有的 Curios 槽位类型总数: {}", stacksHandlerMap.size());

                        for (Map.Entry<String, ICurioStacksHandler> entry : stacksHandlerMap.entrySet()) {
                            String slotId = entry.getKey();
                            ICurioStacksHandler handler = entry.getValue();
                            int slots = handler.getSlots();
                            boolean isVisible = handler.isVisible();

                            if (slotId.contains("wallet") || slotId.contains("charm") || slotId.contains("gun")) {
                                LOGGER.info("[CURIOS-SLOT-DETAIL] 槽位ID: '{}' | 当前槽位数(getSlots): {} | 是否可见(isVisible): {}",
                                        slotId, slots, isVisible);
                            }
                        }
                    } else {
                        LOGGER.warn("[CURIOS-DIAGNOSTIC] 无法从玩家身上获取 Curios Capability 接口！");
                    }

                    // 2. 检查玩家原版 Attribute 属性实例
                    player.getAttributes().getSyncableAttributes().forEach(attribute -> {
                        String desc = attribute.getAttribute().getDescriptionId();
                        if (desc.contains("curios") || desc.contains("wallet")) {
                            AttributeInstance inst = player.getAttribute(attribute.getAttribute());
                            if (inst != null) {
                                LOGGER.info("[CURIOS-ATTR-DETAIL] 属性ID: '{}' | BaseValue: {} | Value: {}",
                                        desc, inst.getBaseValue(), inst.getValue());
                            }
                        }
                    });
                }
            } catch (Throwable t) {
                LOGGER.error("[CURIOS-DIAGNOSTIC] 诊断打印过程捕获异常", t);
            }
        }
    }
}
