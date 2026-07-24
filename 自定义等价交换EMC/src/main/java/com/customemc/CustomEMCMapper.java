package com.customemc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EMCMapper(priority = -999)
public class CustomEMCMapper implements IEMCMapper<NormalizedSimpleStack, Long> {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public String getName() {
        return "CustomEMCMapper";
    }

    @Override
    public String getDescription() {
        return "自定义指定物品EMC，未定价物品默认5555，自适应从创造模式栏全量获取正确NBT实体注册";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper,
                            CommentedFileConfig config,
                            ReloadableServerResources resources,
                            RegistryAccess registryAccess,
                            ResourceManager resourceManager) {

        LOGGER.info("[自定义等价交换EMC] 开始注册自定义EMC价格与创造模式栏全量自适应 NBT 实体...");

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

        // 2. 为全模组未定价基础物品注入默认 5555 EMC 兜底
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

        // 🚀 3. 新思路大绝杀：直接从创造模式栏 (CreativeModeTab) 中全量抓取最真实无误的 NBT 物品实体 (包含 TACZ 全枪包 ELP-45 等实体)
        try {
            LOGGER.info("[自定义等价交换EMC] 启动【创造模式栏全量数据抓取新思路】，自适应提取带 NBT 的具象化实体...");
            Set<String> processedSignatures = new HashSet<>();
            int creativeItemCount = 0;

            for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
                if (tab == null) continue;
                
                // 抓取创造模式栏和搜索栏中包含的所有现成真实 ItemStack
                Collection<ItemStack> items = new HashSet<>();
                try {
                    Collection<ItemStack> searchItems = tab.getSearchTabDisplayItems();
                    if (searchItems != null && !searchItems.isEmpty()) {
                        items.addAll(searchItems);
                    }
                } catch (Throwable ignored) {
                }
                
                try {
                    Collection<ItemStack> displayItems = tab.getDisplayItems();
                    if (displayItems != null && !displayItems.isEmpty()) {
                        items.addAll(displayItems);
                    }
                } catch (Throwable ignored) {
                }

                for (ItemStack stack : items) {
                    if (stack == null || stack.isEmpty()) continue;
                    try {
                        // 唯一签名防重复
                        String signature = stack.getItem().toString() + (stack.hasTag() ? stack.getTag().toString() : "");
                        if (processedSignatures.add(signature)) {
                            NSSItem nss = NSSItem.createItem(stack);
                            mapper.setValueBefore(nss, defaultEmc);
                            creativeItemCount++;
                        }
                    } catch (Throwable ignored) {
                    }
                }
            }
            LOGGER.info("[自定义等价交换EMC] 【创造模式栏新思路大成功】！成功抓取并绑定 {} 个带真实 NBT 的具象化 EMC 实体！", creativeItemCount);
        } catch (Throwable t) {
            LOGGER.warn("[自定义等价交换EMC] 从创造模式栏抓取 NBT 实体时发生提示:", t);
        }
    }
}
