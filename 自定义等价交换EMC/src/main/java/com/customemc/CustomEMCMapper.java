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

@EMCMapper(priority = -999)
public class CustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName() {
        return "CustomEMCMapper";
    }

    @Override
    public String getDescription() {
        return "自定义指定物品EMC，并将所有未设置EMC的物品默认设置为指定数值(默认5555)";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper,
                            CommentedFileConfig config,
                            ReloadableServerResources resources,
                            RegistryAccess registryAccess,
                            ResourceManager resourceManager) {

        LOGGER.info("[自定义等价交换EMC] 开始注册自定义EMC价格与补全未定价物品...");

        ConfigManager.loadConfig();
        long defaultEmc = ConfigManager.getDefaultEMC();
        Map<String, Long> customEmcMap = ConfigManager.getCustomEMCMap();

        if (customEmcMap != null && !customEmcMap.isEmpty()) {
            int customCount = 0;
            for (Map.Entry<String, Long> entry : customEmcMap.entrySet()) {
                String idStr = entry.getKey();
                Long val = entry.getValue();
                if (idStr != null && val != null && val >= 0) {
                    try {
                        ResourceLocation loc = new ResourceLocation(idStr);
                        if (ForgeRegistries.ITEMS.containsKey(loc)) {
                            Item item = ForgeRegistries.ITEMS.getValue(loc);
                            if (item != null) {
                                ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
                                if (itemKey != null && !"minecraft:air".equals(itemKey.toString())) {
                                    mapper.setValueBefore(NSSItem.createItem(item), val);
                                    customCount++;
                                }
                            }
                        } else {
                            LOGGER.warn("[自定义等价交换EMC] 配置文件中的物品未找到: {}", idStr);
                        }
                    } catch (Exception e) {
                        LOGGER.warn("[自定义等价交换EMC] 解析物品ID时出错: {}", idStr);
                    }
                }
            }
            LOGGER.info("[自定义等价交换EMC] 成功自定义设置了 {} 个物品的指定EMC价格！", customCount);
        }

        if (defaultEmc > 0) {
            int defaultCount = 0;
            for (Item item : ForgeRegistries.ITEMS) {
                if (item != null) {
                    ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
                    if (itemKey != null && !"minecraft:air".equals(itemKey.toString())) {
                        mapper.setValueAfter(NSSItem.createItem(item), defaultEmc);
                        defaultCount++;
                    }
                }
            }
            LOGGER.info("[自定义等价交换EMC] 已将全局未定价物品兜底默认EMC设为: {}", defaultEmc);
        }
    }
}
