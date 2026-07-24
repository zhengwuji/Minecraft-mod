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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        return "自定义指定物品EMC，并通过CommonAssetsManager自适应注册所有TACZ枪械具象NBT实体";
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

        // 🚀 3. 绝杀新思路：通过 CommonAssetsManager.getAllGuns() 获取所有枪械的真实 GunId，
        //        用 IGun.setGunId() 写入 NBT，为每一把具象化枪械精准绑定 EMC！
        try {
            LOGGER.info("[自定义等价交换EMC] 启动【CommonAssetsManager 真实 GunId 自适应注册】...");

            // 反射获取 CommonAssetsManager.getInstance()
            Class<?> managerClass = Class.forName("com.tacz.guns.resource.CommonAssetsManager");
            Object manager = managerClass.getMethod("getInstance").invoke(null);

            // 获取所有枪械数据
            Set<?> allGuns = (Set<?>) managerClass.getMethod("getAllGuns").invoke(manager);

            // 获取 MODERN_KINETIC_GUN Item
            Class<?> modItemsClass = Class.forName("com.tacz.guns.init.ModItems");
            Object modernKineticGunObj = modItemsClass.getField("MODERN_KINETIC_GUN").get(null);
            Item gunItem = (Item) modernKineticGunObj.getClass().getMethod("get").invoke(modernKineticGunObj);

            // 获取 IGun 接口
            Class<?> iGunClass = Class.forName("com.tacz.guns.api.item.IGun");
            java.lang.reflect.Method getIGunOrNull = iGunClass.getMethod("getIGunOrNull", ItemStack.class);
            java.lang.reflect.Method setGunId = iGunClass.getMethod("setGunId", ItemStack.class, ResourceLocation.class);

            // 获取 Map.Entry 的 getKey
            int gunCount = 0;
            for (Object entry : allGuns) {
                java.util.Map.Entry<?, ?> mapEntry = (java.util.Map.Entry<?, ?>) entry;
                ResourceLocation gunId = (ResourceLocation) mapEntry.getKey();

                try {
                    // 创建一个新 ItemStack
                    ItemStack stack = new ItemStack(gunItem);

                    // 用 IGun 接口写入 GunId NBT（这是 TACZ 官方的标准做法）
                    Object iGun = getIGunOrNull.invoke(null, stack);
                    if (iGun != null) {
                        setGunId.invoke(iGun, stack, gunId);

                        // 用这个带完整 GunId NBT 的 ItemStack 创建 NSS 并注册 EMC
                        NSSItem nss = NSSItem.createItem(stack);
                        mapper.setValueBefore(nss, defaultEmc);
                        gunCount++;
                        LOGGER.debug("[自定义等价交换EMC] 注册枪械 EMC: {} -> {}", gunId, defaultEmc);
                    }
                } catch (Throwable t) {
                    LOGGER.warn("[自定义等价交换EMC] 注册枪械 {} 失败: {}", gunId, t.getMessage());
                }
            }
            LOGGER.info("[自定义等价交换EMC] 【CommonAssetsManager 真实GunId注册大成功】！共注册 {} 把枪械具象化实体的 EMC！", gunCount);
        } catch (Throwable t) {
            LOGGER.warn("[自定义等价交换EMC] CommonAssetsManager 自适应注册枪械时发生异常: ", t);
        }
    }
}
