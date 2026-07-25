package com.grim.workbenches;

import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, "grimpack");
    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH = MENUS.register("workbench", () -> IForgeMenuType.create((windowId, inv, data) -> {
        int multiplier = data.readInt();
        return new WorkbenchMenu(windowId, inv, ContainerLevelAccess.NULL, multiplier);
    }));
}