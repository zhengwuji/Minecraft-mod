package com.antigravity.foodbuffbag.event;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.antigravity.foodbuffbag.config.FoodBuffConfig;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = FoodBuffBag.MOD_ID)
public class FoodBuffHandler {

    // 1. 挂载 Capability 到玩家身上
    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(FoodBuffProvider.FOOD_BUFF_CAP).isPresent()) {
                event.addCapability(new ResourceLocation(FoodBuffBag.MOD_ID, "food_buff_inventory"), new FoodBuffProvider());
            }
        }
    }

    // 2. 玩家死亡/重用/跨维度克隆时，继承所有随身仓数据
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();

        oldPlayer.reviveCaps();
        oldPlayer.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(oldCap -> {
            newPlayer.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(newCap -> {
                newCap.deserializeNBT(oldCap.serializeNBT());
            });
        });
        oldPlayer.invalidateCaps();
    }

    // 3. 实时 ServerPlayer Tick：全仓扫描，实现【同类附魔与同类BUFF等级无上限累加叠加】
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;

            int interval = FoodBuffConfig.SCAN_INTERVAL.get();
            if (player.tickCount % interval == 0) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    int totalSlots = cap.getSlots();
                    int emptyStreak = 0;

                    // BUFF 等级累加计算器 <MobEffect, 累计等级Level>
                    Map<MobEffect, Integer> accumulatedLevels = new HashMap<>();

                    for (int i = 0; i < totalSlots; i++) {
                        ItemStack stack = cap.getStackInSlot(i);
                        if (stack.isEmpty()) {
                            emptyStreak++;
                            if (emptyStreak > 108) {
                                break;
                            }
                            continue;
                        }

                        emptyStreak = 0;
                        // 收集该物品/附魔装备提供的全套 BUFF 并累加等级
                        collectStackItemEffects(player, stack, accumulatedLevels);
                    }

                    // 将全仓累加后的无上限叠加 BUFF 统一向玩家应用刷新
                    applyAccumulatedEffects(player, accumulatedLevels);
                });
            }
        }
    }

    private static void collectStackItemEffects(ServerPlayer player, ItemStack stack, Map<MobEffect, Integer> accumulatedLevels) {
        boolean filterHarmful = FoodBuffConfig.FILTER_HARMFUL.get();

        // A. 食物属性 (FoodProperties) 效果解析与等级累加
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food != null) {
            for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
                MobEffectInstance origEffect = pair.getFirst();
                if (origEffect != null) {
                    if (filterHarmful && origEffect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        continue;
                    }
                    accumulateEffectLevel(accumulatedLevels, origEffect.getEffect(), origEffect.getAmplifier() + 1);
                }
            }
        }

        // B. 迷之炖菜 (SuspiciousStewItem)
        if (stack.getItem() instanceof SuspiciousStewItem) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Effects", 9)) {
                ListTag list = tag.getList("Effects", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag effectTag = list.getCompound(i);
                    byte effectId = effectTag.getByte("EffectId");
                    MobEffect effect = MobEffect.byId(effectId);
                    if (effect != null) {
                        if (filterHarmful && effect.getCategory() == MobEffectCategory.HARMFUL) {
                            continue;
                        }
                        accumulateEffectLevel(accumulatedLevels, effect, 1);
                    }
                }
            }
        }

        // C. 金苹果与附魔金苹果强化 (效果可叠加)
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, 2);      // 生命恢复 II (+2级)
            accumulateEffectLevel(accumulatedLevels, MobEffects.ABSORPTION, 4);        // 伤害吸收 IV (+4级)
            accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, 1);  // 抗性提升 I (+1级)
            accumulateEffectLevel(accumulatedLevels, MobEffects.FIRE_RESISTANCE, 1);    // 抗火 I (+1级)
        } else if (stack.is(Items.GOLDEN_APPLE)) {
            accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, 2);      // 生命恢复 II (+2级)
            accumulateEffectLevel(accumulatedLevels, MobEffects.ABSORPTION, 1);        // 伤害吸收 I (+1级)
        }

        // D. 药水与带有药水效果的物品解析
        for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
            if (filterHarmful && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                continue;
            }
            accumulateEffectLevel(accumulatedLevels, effect.getEffect(), effect.getAmplifier() + 1);
        }

        // E. 附魔装备与附魔物品全效解析（同类附魔等级全量累加叠加！）
        if (stack.isEnchanted() || (stack.hasTag() && stack.getTag().contains("Enchantments", 9))) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchant = entry.getKey();
                int level = entry.getValue();
                if (enchant == null || level <= 0) continue;

                // 1. 保护防具类附魔 -> 伤害减免与抗性提升（等级全量累加）
                if (enchant == Enchantments.ALL_DAMAGE_PROTECTION || enchant == Enchantments.FIRE_PROTECTION
                        || enchant == Enchantments.BLAST_PROTECTION || enchant == Enchantments.PROJECTILE_PROTECTION) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, level);
                    if (enchant == Enchantments.FIRE_PROTECTION) {
                        accumulateEffectLevel(accumulatedLevels, MobEffects.FIRE_RESISTANCE, 1);
                    }
                }
                // 2. 武器攻击类附魔 (锋利/力量/亡灵杀手/节肢杀手) -> 力量 BUFF（等级全量累加）
                else if (enchant == Enchantments.SHARPNESS || enchant == Enchantments.POWER_ARROWS
                        || enchant == Enchantments.SMITE || enchant == Enchantments.BANE_OF_ARTHROPODS) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_BOOST, level);
                }
                // 3. 水下类附魔 (水下呼吸/深海游将)
                else if (enchant == Enchantments.RESPIRATION || enchant == Enchantments.AQUA_AFFINITY) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.WATER_BREATHING, 1);
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DIG_SPEED, level);
                }
                // 4. 移动与速度类附魔 (深海游将/灵魂疾行/迅捷潜行)
                else if (enchant == Enchantments.DEPTH_STRIDER || enchant == Enchantments.SOUL_SPEED
                        || enchant == Enchantments.SWIFT_SNEAK) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.MOVEMENT_SPEED, level);
                }
                // 5. 摔落保护 (缓降与抗性)
                else if (enchant == Enchantments.FALL_PROTECTION) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.SLOW_FALLING, 1);
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, level);
                }
                // 6. 挖掘效率 (急迫)
                else if (enchant == Enchantments.BLOCK_EFFICIENCY) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DIG_SPEED, level);
                }
                // 7. 幸运/时运/抢夺/海之眷顾 -> 幸运 BUFF（等级全量累加）
                else if (enchant == Enchantments.MOB_LOOTING || enchant == Enchantments.BLOCK_FORTUNE
                        || enchant == Enchantments.FISHING_LUCK) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.LUCK, level);
                }
                // 8. 经验修补与耐久 -> 自动生命恢复（等级全量累加）并修复装备
                else if (enchant == Enchantments.MENDING || enchant == Enchantments.UNBREAKING) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, level);
                    if (enchant == Enchantments.MENDING && stack.isDamaged() && player.getRandom().nextFloat() < 0.3F) {
                        stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                }
                // 9. 任意其它原版及 MOD 附魔装备通用保底提升
                else {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, level);
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, level);
                }
            }
        }
    }

    private static void accumulateEffectLevel(Map<MobEffect, Integer> accumulatedLevels, MobEffect effect, int levelsToAdd) {
        if (effect == null || levelsToAdd <= 0) return;
        accumulatedLevels.put(effect, accumulatedLevels.getOrDefault(effect, 0) + levelsToAdd);
    }

    private static void applyAccumulatedEffects(ServerPlayer player, Map<MobEffect, Integer> accumulatedLevels) {
        for (Map.Entry<MobEffect, Integer> entry : accumulatedLevels.entrySet()) {
            MobEffect effect = entry.getKey();
            int totalLevels = entry.getValue();
            if (effect == null || totalLevels <= 0) continue;

            // 转换总 Level 为 Minecraft 药水倍率 Amplifier (Level 1 -> Amp 0, Level 10 -> Amp 9)
            int totalAmplifier = totalLevels - 1;

            MobEffectInstance existing = player.getEffect(effect);
            if (existing == null || existing.getAmplifier() != totalAmplifier || existing.getDuration() < 100) {
                MobEffectInstance newEffect = new MobEffectInstance(
                        effect,
                        300,            // 15 秒持续刷新
                        totalAmplifier,
                        false,          // ambient
                        true,           // visible
                        true            // showIcon
                );
                player.addEffect(newEffect);
            }
        }
    }
}
