package dev.toliner.reinforcedtools;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class RepairKitItem extends Item {
    private final ReinforcedToolMaterial material;

    public RepairKitItem(ReinforcedToolMaterial material, Item.Properties properties) {
        super(properties);
        this.material = material;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (hand != InteractionHand.OFF_HAND) {
            return InteractionResultHolder.pass(itemstack);
        }

        ItemStack toolStack = player.getMainHandItem();
        if (!(toolStack.getItem() instanceof ReinforcedTool reinforcedTool)
            || reinforcedTool.reinforcedMaterial() != material
            || !toolStack.isDamaged()) {
            return InteractionResultHolder.pass(itemstack);
        }

        if (!level.isClientSide()) {
            int repairAmount = Math.max(1, Math.round(toolStack.getMaxDamage() * 0.25F));
            toolStack.setDamageValue(toolStack.getDamageValue() - repairAmount);
            if (!player.getAbilities().instabuild) {
                itemstack.shrink(1);
            }
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
