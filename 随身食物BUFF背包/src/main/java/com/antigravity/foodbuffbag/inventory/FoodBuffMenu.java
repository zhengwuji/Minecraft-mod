package com.antigravity.foodbuffbag.inventory;

import com.antigravity.foodbuffbag.FoodBuffBag;
import com.antigravity.foodbuffbag.capability.FoodBuffProvider;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import org.jetbrains.annotations.NotNull;

public class FoodBuffMenu extends AbstractContainerMenu {
    private final ItemStackHandler handler;
    private int currentPage = 0;

    // 客户端构造函数
    public FoodBuffMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, new ItemStackHandler(FoodBuffProvider.TOTAL_SLOTS));
    }

    // 服务端构造函数
    public FoodBuffMenu(int containerId, Inventory playerInventory, ItemStackHandler handler) {
        super(FoodBuffBag.FOOD_BUFF_MENU.get(), containerId);
        this.handler = handler;

        // 1. 添加当前页的 54 个 Slot (6 行 x 9 列)，使用动态页码槽位包装
        for (int row = 0; row < 6; ++row) {
            for (int col = 0; col < 9; ++col) {
                int slotIndexOnPage = col + row * 9;
                this.addSlot(new DynamicPageSlot(handler, slotIndexOnPage, 8 + col * 18, 18 + row * 18));
            }
        }

        // 2. 添加玩家主背包 (3 行 x 9 列)
        int playerInvY = 140;
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        // 3. 添加玩家快捷栏 (9 列)
        int hotbarY = 198;
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, hotbarY));
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void changePage(int delta) {
        int newPage = currentPage + delta;
        if (newPage >= 0 && newPage < FoodBuffProvider.MAX_PAGES) {
            this.currentPage = newPage;
            this.broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public static boolean isSupportedItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        // 1. 食物
        if (stack.isEdible() || stack.getItem().isEdible()) return true;
        // 2. 附魔物品与装备 (含有 Enchantments NBT 或已附魔)
        if (stack.isEnchanted() || (stack.hasTag() && stack.getTag().contains("Enchantments", 9))) return true;
        // 3. 常见防具、武器、工具与装备
        if (stack.getItem() instanceof net.minecraft.world.item.ArmorItem ||
            stack.getItem() instanceof net.minecraft.world.item.SwordItem ||
            stack.getItem() instanceof net.minecraft.world.item.DiggerItem ||
            stack.getItem() instanceof net.minecraft.world.item.BowItem ||
            stack.getItem() instanceof net.minecraft.world.item.ShieldItem ||
            stack.getItem() instanceof net.minecraft.world.item.CrossbowItem ||
            stack.getItem() instanceof net.minecraft.world.item.TridentItem ||
            stack.getItem() instanceof net.minecraft.world.item.ShearsItem ||
            stack.getItem() instanceof net.minecraft.world.item.FishingRodItem) return true;
        // 4. 含有药水效果或属性修饰符的物品
        if (stack.hasTag() && (stack.getTag().contains("AttributeModifiers", 9) ||
                stack.getTag().contains("Potion", 8) ||
                stack.getTag().contains("CustomPotionEffects", 9))) return true;

        return !stack.isEmpty();
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index < 54) {
                // 从当前食物/装备背包页移动到玩家背包
                if (!this.moveItemStackTo(itemstack1, 54, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 从玩家背包快捷放入当前背包页（支持食物与附魔装备）
                if (isSupportedItem(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, 0, 54, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    // 自定义动态页码 Slot 映射类
    private class DynamicPageSlot extends SlotItemHandler {
        private final int pageSlotIndex;

        public DynamicPageSlot(IItemHandler itemHandler, int pageSlotIndex, int xPosition, int yPosition) {
            super(itemHandler, pageSlotIndex, xPosition, yPosition);
            this.pageSlotIndex = pageSlotIndex;
        }

        private int getActualSlotIndex() {
            return currentPage * FoodBuffProvider.SLOTS_PER_PAGE + pageSlotIndex;
        }

        @Override
        public @NotNull ItemStack getItem() {
            return getItemHandler().getStackInSlot(getActualSlotIndex());
        }

        @Override
        public void set(@NotNull ItemStack stack) {
            ((ItemStackHandler) getItemHandler()).setStackInSlot(getActualSlotIndex(), stack);
            this.setChanged();
        }

        @Override
        public void setChanged() {
            super.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return isSupportedItem(stack);
        }

        @Override
        public @NotNull ItemStack remove(int amount) {
            return getItemHandler().extractItem(getActualSlotIndex(), amount, false);
        }
    }
}
