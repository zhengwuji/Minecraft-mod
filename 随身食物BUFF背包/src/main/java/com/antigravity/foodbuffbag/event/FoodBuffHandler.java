package com.antigravity.foodbuffbag.event;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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

    // 2. 玩家死亡/重用/跨维度克隆时，继承所有随身食物仓数据
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

    // 3. 实时 ServerPlayer Tick，全量遍历 100 页 (5400 槽位) 保持注入所有食物 BUFF
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
            ServerPlayer player = (ServerPlayer) event.player;

            // 每 10 Tick (0.5秒) 全量扫描一次
            if (player.tickCount % 10 == 0) {
                player.getCapability(FoodBuffProvider.FOOD_BUFF_CAP).ifPresent(cap -> {
                    int totalSlots = cap.getSlots();
                    for (int i = 0; i < totalSlots; i++) {
                        ItemStack stack = cap.getStackInSlot(i);
                        if (stack.isEmpty()) continue;

                        applyStackFoodEffects(player, stack);
                    }
                });
            }
        }
    }

    private static void applyStackFoodEffects(ServerPlayer player, ItemStack stack) {
        // A. 通用 FoodProperties 药水效果解析（涵盖大部分原生及模组食物）
        FoodProperties food = stack.getItem().getFoodProperties(stack, player);
        if (food != null) {
            for (Pair<MobEffectInstance, Float> pair : food.getEffects()) {
                MobEffectInstance origEffect = pair.getFirst();
                if (origEffect != null) {
                    addOrRefreshInfiniteEffect(player, origEffect.getEffect(), origEffect.getAmplifier());
                }
            }
        }

        // B. 迷之炖菜 (SuspiciousStewItem) 特殊效果处理
        if (stack.getItem() instanceof SuspiciousStewItem) {
            CompoundTag tag = stack.getTag();
            if (tag != null && tag.contains("Effects", 9)) {
                ListTag list = tag.getList("Effects", 10);
                for (int i = 0; i < list.size(); i++) {
                    CompoundTag effectTag = list.getCompound(i);
                    byte effectId = effectTag.getByte("EffectId");
                    MobEffect effect = MobEffect.byId(effectId);
                    if (effect != null) {
                        addOrRefreshInfiniteEffect(player, effect, 0);
                    }
                }
            }
        }

        // C. 经典金苹果与附魔金苹果兜底强化
        if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, 1);    // 生命恢复 II
            addOrRefreshInfiniteEffect(player, MobEffects.ABSORPTION, 3);       // 伤害吸收 IV
            addOrRefreshInfiniteEffect(player, MobEffects.DAMAGE_RESISTANCE, 0); // 抗性提升 I
            addOrRefreshInfiniteEffect(player, MobEffects.FIRE_RESISTANCE, 0);   // 抗火 I
        } else if (stack.is(Items.GOLDEN_APPLE)) {
            addOrRefreshInfiniteEffect(player, MobEffects.REGENERATION, 1);    // 生命恢复 II
            addOrRefreshInfiniteEffect(player, MobEffects.ABSORPTION, 0);       // 伤害吸收 I
        }
    }

    private static void addOrRefreshInfiniteEffect(ServerPlayer player, MobEffect effect, int amplifier) {
        if (effect == null) return;

        MobEffectInstance existing = player.getEffect(effect);
        // 如果玩家身上没有这个效果，或者已有效果等级小于或剩余时长较短，则刷新赋予持续时间为 300 Tick (15秒) 的 BUFF
        if (existing == null || existing.getAmplifier() < amplifier || existing.getDuration() < 100) {
            MobEffectInstance newEffect = new MobEffectInstance(
                    effect,
                    300,            // 15 秒持续时间，每0.5秒刷新
                    amplifier,
                    false,          // ambient
                    true,           // visible
                    true            // showIcon
            );
            player.addEffect(newEffect);
        }
    }
}
