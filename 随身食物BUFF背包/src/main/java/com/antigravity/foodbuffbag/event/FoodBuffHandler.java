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

    // 3. 实时 ServerPlayer Tick，同时支持【食物BUFF】与【附魔装备BUFF】
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;

            int interval = FoodBuffConfig.SCAN_INTERVAL.get();
            if (player.tickCount % interval == 0) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    int totalSlots = cap.getSlots();
                    int emptyStreak = 0;

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
                        // 应用食物及附魔装备的全套 BUFF 效果
                        applyStackItemEffects(player, stack);
                    }
                });
            }
        }
    }

    private static void applyStackItemEffects(ServerPlayer player, ItemStack stack) {
        boolean filterHarmful = FoodBuffConfig.FILTER_HARMFUL.get();

        // A. 食物属性 (FoodProperties) 效果解析
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food != null) {
            for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
                MobEffectInstance origEffect = pair.getFirst();
                if (origEffect != null) {
                    if (filterHarmful && origEffect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        continue;
                    }
                    addOrRefreshInfiniteEffect(player, origEffect.getEffect(), origEffect.getAmplifier());
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
                        addOrRefreshInfiniteEffect(player, effect, 0);
                    }
                }
            }
        }

        // C. 金苹果与附魔金苹果强化
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, 1);    // 生命恢复 II
            addOrRefreshInfiniteEffect(player, MobEffects.ABSORPTION, 3);       // 伤害吸收 IV
            addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_RESISTANCE, 0); // 抗性提升 I
            addOrRefreshInfiniteEffect(player, MobEffects.FIRE_RESISTANCE, 0);   // 抗火 I
        } else if (stack.is(Items.GOLDEN_APPLE)) {
            addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, 1);    // 生命恢复 II
            addOrRefreshInfiniteEffect(player, MobEffects.ABSORPTION, 0);       // 伤害吸收 I
        }

        // D. 药水与带有药水效果的物品解析
        for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
            if (filterHarmful && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                continue;
            }
            addOrRefreshInfiniteEffect(player, effect.getEffect(), effect.getAmplifier());
        }

        // E. 附魔装备与附魔物品全效解析（支持原版及所有 MOD 的附魔装备）
        if (stack.isEnchanted() || (stack.hasTag() && stack.getTag().contains("Enchantments", 9))) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchant = entry.getKey();
                int level = entry.getValue();
                if (enchant == null || level <= 0) continue;

                int amp = Math.max(0, level - 1);

                // 1. 保护防具类附魔 -> 伤害减免与抗性提升
                if (enchant == Enchantments.ALL_DAMAGE_PROTECTION || enchant == Enchantments.FIRE_PROTECTION
                        || enchant == Enchantments.BLAST_PROTECTION || enchant == Enchantments.PROJECTILE_PROTECTION) {
                    addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_RESISTANCE, Math.min(amp, 4));
                    if (enchant == Enchantments.FIRE_PROTECTION) {
                        addOrRefreshInfiniteEffect(player, MobEffects.FIRE_RESISTANCE, 0);
                    }
                }
                // 2. 武器攻击类附魔 (锋利/力量/亡灵杀手/节肢杀手) -> 攻击力与力量 BUFF
                else if (enchant == Enchantments.SHARPNESS || enchant == Enchantments.POWER_ARROWS
                        || enchant == Enchantments.SMITE || enchant == Enchantments.BANE_OF_ARTHROPODS) {
                    addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_BOOST, Math.min(amp, 4));
                }
                // 3. 水下类附魔 (水下呼吸/深海游将)
                else if (enchant == Enchantments.RESPIRATION || enchant == Enchantments.AQUA_AFFINITY) {
                    addOrRefreshInfiniteEffect(player, MobEffects.WATER_BREATHING, 0);
                    addOrRefreshInfiniteEffect(player, MobEffects.DIG_SPEED, amp);
                }
                // 4. 移动与速度类附魔 (深海游将/灵魂疾行/迅捷潜行)
                else if (enchant == Enchantments.DEPTH_STRIDER || enchant == Enchantments.SOUL_SPEED
                        || enchant == Enchantments.SWIFT_SNEAK) {
                    addOrRefreshInfiniteEffect(player, MobEffects.MOVEMENT_SPEED, amp);
                }
                // 5. 摔落保护 (缓降)
                else if (enchant == Enchantments.FALL_PROTECTION) {
                    addOrRefreshInfiniteEffect(player, MobEffects.SLOW_FALLING, 0);
                    addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_RESISTANCE, 0);
                }
                // 6. 挖掘效率 (急迫)
                else if (enchant == Enchantments.BLOCK_EFFICIENCY) {
                    addOrRefreshInfiniteEffect(player, MobEffects.DIG_SPEED, amp);
                }
                // 7. 幸运/时运/抢夺/海之眷顾 -> 幸运 BUFF
                else if (enchant == Enchantments.MOB_LOOTING || enchant == Enchantments.BLOCK_FORTUNE
                        || enchant == Enchantments.FISHING_LUCK) {
                    addOrRefreshInfiniteEffect(player, MobEffects.LUCK, amp);
                }
                // 8. 经验修补与耐久 -> 自动生命恢复
                else if (enchant == Enchantments.MENDING || enchant == Enchantments.UNBREAKING) {
                    addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, amp);
                    // 自动缓慢修补背包内的受损装备
                    if (enchant == Enchantments.MENDING && stack.isDamaged() && player.getRandom().nextFloat() < 0.3F) {
                        stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                }
                // 9. 任意其它原版及 MOD 附魔装备通用保底提升
                else {
                    addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, 0);
                    addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_RESISTANCE, 0);
                }
            }
        }
    }

    private static void addOrRefreshInfiniteEffect(ServerPlayer player, MobEffect effect, int amplifier) {
        if (effect == null) return;

        MobEffectInstance existing = player.getEffect(effect);
        if (existing == null || existing.getAmplifier() < amplifier || existing.getDuration() < 100) {
            MobEffectInstance newEffect = new MobEffectInstance(
                    effect,
                    300,            // 15 秒持续刷新
                    amplifier,
                    false,          // ambient
                    true,           // visible
                    true            // showIcon
            );
            player.addEffect(newEffect);
        }
    }
}
