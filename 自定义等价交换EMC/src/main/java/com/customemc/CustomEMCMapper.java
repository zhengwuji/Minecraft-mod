package com.customemc;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import moze_intel.projecte.api.mapper.EMCMapper;
import moze_intel.projecte.api.mapper.IEMCMapper;
import moze_intel.projecte.api.mapper.collector.IMappingCollector;
import moze_intel.projecte.api.nss.NSSItem;
import moze_intel.projecte.api.nss.NormalizedSimpleStack;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
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
        return "自定义指定物品EMC，未定价物品默认5555，自适应绑定TACZ枪包等NBT物品";
    }

    @Override
    public void addMappings(IMappingCollector<NormalizedSimpleStack, Long> mapper,
                            CommentedFileConfig config,
                            ReloadableServerResources resources,
                            RegistryAccess registryAccess,
                            ResourceManager resourceManager) {

        LOGGER.info("[自定义等价交换EMC] 开始注册自定义EMC价格与全模组自适应兜底价格...");

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

        // 2. 为全模组未定价物品注入默认 5555 EMC 兜底
        int defaultCount = 0;
        for (Item item : ForgeRegistries.ITEMS) {
            ResourceLocation loc = ForgeRegistries.ITEMS.getKey(item);
            if (loc == null) continue;
            String idStr = loc.toString();

            // 如果该物品没有在 custom_emc.json 中指定过，则注入默认 EMC
            if (customEmcMap == null || !customEmcMap.containsKey(idStr)) {
                try {
                    mapper.setValueBefore(NSSItem.createItem(item), defaultEmc);
                    defaultCount++;
                } catch (Exception ignored) {
                }
            }
        }
        LOGGER.info("[自定义等价交换EMC] 成功为 {} 个全模组物品注入默认 {} EMC！", defaultCount, defaultEmc);

        // 🚀 3. TACZ 枪械模组自适应绑定：通过反射提取全枪包 GunId，为每把带 NBT 的具体枪械注册精确 EMC 实体
        if (ModList.get().isLoaded("tacz")) {
            try {
                LOGGER.info("[自定义等价交换EMC] 侦测到 TACZ 枪械模组，开始自适应反射提取全枪包 NBT 注册...");
                Item gunItem = ForgeRegistries.ITEMS.getValue(new ResourceLocation("tacz", "modern_kinetic_gun"));
                if (gunItem != null) {
                    Set<ResourceLocation> gunIds = new HashSet<>();

                    // 抓取 ClientIndexManager / CommonIndexManager
                    String[] managerClassNames = {
                            "com.tacz.guns.client.resource.ClientIndexManager",
                            "com.tacz.guns.resource.CommonIndexManager",
                            "com.tacz.guns.resource.ServerIndexManager"
                    };

                    for (String clsName : managerClassNames) {
                        try {
                            Class<?> clazz = Class.forName(clsName);
                            Field field = clazz.getField("GUN_INDEX");
                            Map<?, ?> map = (Map<?, ?>) field.get(null);
                            if (map != null) {
                                for (Object key : map.keySet()) {
                                    if (key instanceof ResourceLocation loc) {
                                        gunIds.add(loc);
                                    }
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    }

                    LOGGER.info("[自定义等价交换EMC] 自适应反射共提取到 {} 个 TACZ 枪械 GunId 索引！", gunIds.size());
                    for (ResourceLocation gunId : gunIds) {
                        try {
                            CompoundTag tag = new CompoundTag();
                            tag.putString("GunId", gunId.toString());
                            mapper.setValueBefore(NSSItem.createItem(gunItem, tag), defaultEmc);
                        } catch (Throwable ignored) {
                        }
                    }
                    LOGGER.info("[自定义等价交换EMC] 成功为 TACZ 全枪包枪械绑定带有 NBT (GunId) 的 EMC 实体！");
                }
            } catch (Throwable t) {
                LOGGER.warn("[自定义等价交换EMC] 自动遍历 TACZ 枪包发生非致命提示:", t);
            }
        }
    }
}
