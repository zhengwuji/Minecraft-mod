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

    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(FoodBuffProvider.FOOD_BUFF_CAP).isPresent()) {
                event.addCapability(new ResourceLocation(FoodBuffBag.MOD_ID, "food_buff_inventory"), new FoodBuffProvider());
            }
        }
    }

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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;

            int interval = FoodBuffConfig.SCAN_INTERVAL.get();
            if (player.tickCount % interval == 0) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    int totalSlots = cap.getSlots();
                    int emptyStreak = 0;

                    Map<MobEffect, Integer> accumulatedLevels = new HashMap<>();
                    boolean hasFlightItem = false;

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

                        String itemClassName = stack.getItem().getClass().getName().toLowerCase();
                        String itemDescId = stack.getItem().getDescriptionId().toLowerCase();

                        if (itemClassName.contains("cloud") || itemClassName.contains("fly") || itemDescId.contains("cloud") || itemDescId.contains("fly") || itemDescId.contains("flight")) {
                            hasFlightItem = true;
                        }

                        try {
                            stack.getItem().inventoryTick(stack, player.level(), player, i, true);
                        } catch (Throwable ignored) {
                        }

                        collectStackItemEffects(player, stack, accumulatedLevels);
                    }

                    if (hasFlightItem) {
                        if (!player.getAbilities().mayfly) {
                            player.getAbilities().mayfly = true;
                            player.onUpdateAbilities();
                        }
                    }

                    applyAccumulatedEffects(player, accumulatedLevels);
                });
            }
        }
    }

    private static void collectStackItemEffects(ServerPlayer player, ItemStack stack, Map<MobEffect, Integer> accumulatedLevels) {
        boolean filterHarmful = FoodBuffConfig.FILTER_HARMFUL.get();
        String itemClassName = stack.getItem().getClass().getName().toLowerCase();
        String itemDescId = stack.getItem().getDescriptionId().toLowerCase();

        if (itemClassName.contains("inventorypets") || itemClassName.contains("pet") || itemDescId.contains("pet")) {
            CompoundTag tag = stack.getOrCreateTag();
            tag.putLong("LastEatTime", player.level().getGameTime());
            tag.putInt("InBagDuration", 99999);

            accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, 1);
            accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, 1);
            accumulateEffectLevel(accumulatedLevels, MobEffects.NIGHT_VISION, 1);
        }

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

        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, 2);
            accumulateEffectLevel(accumulatedLevels, MobEffects.ABSORPTION, 4);
            accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, 1);
            accumulateEffectLevel(accumulatedLevels, MobEffects.FIRE_RESISTANCE, 1);
        } else if (stack.is(Items.GOLDEN_APPLE)) {
            accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, 2);
            accumulateEffectLevel(accumulatedLevels, MobEffects.ABSORPTION, 1);
        }

        for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
            if (filterHarmful && effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                continue;
            }
            accumulateEffectLevel(accumulatedLevels, effect.getEffect(), effect.getAmplifier() + 1);
        }

        if (stack.isEnchanted() || (stack.hasTag() && stack.getTag().contains("Enchantments", 9))) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchant = entry.getKey();
                int level = entry.getValue();
                if (enchant == null || level <= 0) continue;

                if (enchant == Enchantments.ALL_DAMAGE_PROTECTION || enchant == Enchantments.FIRE_PROTECTION
                        || enchant == Enchantments.BLAST_PROTECTION || enchant == Enchantments.PROJECTILE_PROTECTION) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, level);
                    if (enchant == Enchantments.FIRE_PROTECTION) {
                        accumulateEffectLevel(accumulatedLevels, MobEffects.FIRE_RESISTANCE, 1);
                    }
                } else if (enchant == Enchantments.SHARPNESS || enchant == Enchantments.POWER_ARROWS
                        || enchant == Enchantments.SMITE || enchant == Enchantments.BANE_OF_ARTHROPODS) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_BOOST, level);
                } else if (enchant == Enchantments.RESPIRATION || enchant == Enchantments.AQUA_AFFINITY) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.WATER_BREATHING, 1);
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DIG_SPEED, level);
                } else if (enchant == Enchantments.DEPTH_STRIDER || enchant == Enchantments.SOUL_SPEED
                        || enchant == Enchantments.SWIFT_SNEAK) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.MOVEMENT_SPEED, level);
                } else if (enchant == Enchantments.FALL_PROTECTION) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.SLOW_FALLING, 1);
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DAMAGE_RESISTANCE, level);
                } else if (enchant == Enchantments.BLOCK_EFFICIENCY) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.DIG_SPEED, level);
                } else if (enchant == Enchantments.MOB_LOOTING || enchant == Enchantments.BLOCK_FORTUNE
                        || enchant == Enchantments.FISHING_LUCK) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.LUCK, level);
                } else if (enchant == Enchantments.MENDING || enchant == Enchantments.UNBREAKING) {
                    accumulateEffectLevel(accumulatedLevels, MobEffects.REGENERATION, level);
                    if (enchant == Enchantments.MENDING && stack.isDamaged() && player.getRandom().nextFloat() < 0.3F) {
                        stack.setDamageValue(stack.getDamageValue() - 1);
                    }
                } else {
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

            int totalAmplifier = totalLevels - 1;

            MobEffectInstance existing = player.getEffect(effect);
            if (existing == null || existing.getAmplifier() != totalAmplifier || existing.getDuration() < 100) {
                MobEffectInstance newEffect = new MobEffectInstance(
                        effect,
                        300,
                        totalAmplifier,
                        false,
                        true,
                        true
                );
                player.addEffect(newEffect);
            }
        }
    }
}
