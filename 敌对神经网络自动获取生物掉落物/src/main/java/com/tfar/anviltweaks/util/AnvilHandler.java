package com.tfar.anviltweaks.util;

import net.minecraftforge.items.ItemStackHandler;

public class AnvilHandler extends ItemStackHandler {
    public AnvilHandler(int size) {
        super(size);
    }

    @Override
    protected void onContentsChanged(int slot) {
        super.onContentsChanged(slot);
    }
}
