package com.customemc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@EMCMapper(priority = 999)
public class CustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName() {
        return "CustomEMCMapper";
    }

    @Override
    public String getDescription() {
        return "自定义指定物品EMC，为未定价物品提供默认5555 EMC基础定价支持";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper,
                            CommentedFileConfig config,
                            ReloadableServerResources resources,
                            RegistryAccess registryAccess,
                            ResourceManager resourceManager) {

        LOGGER.info("[自定义等价交换EMC] 开始注册自定义EMC价格...");

        ConfigManager.loadConfig();
        long defaultEmc = ConfigManager.getDefaultEMC();
        Map<String, Long> customEmcMap = ConfigManager.getCustomEMCMap();

        // 1. 注册配置文件中的指定价格
        if (customEmcMap != null && !customEmcMap.isEmpty()) {
            int customCount = 0;
            for (Map.Entry<String, Long> entry : customEmcMap.entrySet()) {
                String idStr = entry.getKey();
                Long val = entry.getValue();
                if (idStr != null && val != null && val > 0) {
                    try {
                        ResourceLocation loc = new ResourceLocation(idStr);
                        Item item = ForgeRegistries.ITEMS.getValue(loc);
                        if (item != null && item != net.minecraft.world.item.Items.AIR) {
                            mapper.setValueBefore(NSSItem.createItem(item), val);
                            customCount++;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("[自定义等价交换EMC] 解析指定物品ID失败: {}", idStr);
                    }
                }
            }
            LOGGER.info("[自定义等价交换EMC] 成功注册 {} 个自定义指定EMC物品！", customCount);
        }

        // 2. 为全模组未定价基础物品注入默认 5555 EMC 兜底（包括 TACZ 枪械、配件、弹药）
        int defaultCount = 0;
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item);
            if (loc == null) continue;
            String idStr = loc.toString();

            if (customEmcMap == null || !customEmcMap.containsKey(idStr)) {
                try {
                    mapper.setValueBefore(NSSItem.createItem(item), defaultEmc);
                    defaultCount++;
                } catch (Exception ignored) {
                }
            }
        }
        LOGGER.info("[自定义等价交换EMC] 成功为 {} 个全模组基础物品注入默认 {} EMC！", defaultCount, defaultEmc);
    }
}
