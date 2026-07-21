/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.inventory.ContainerLevelAccess
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraftforge.common.extensions.IForgeMenuType
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.grim.workbenches;

import com.grim.workbenches.WorkbenchMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.MENU_TYPES, (String)"grimpack");
    public static final RegistryObject<MenuType<WorkbenchMenu>> WORKBENCH = MENUS.register("workbench", () -> IForgeMenuType.create((windowId, inv, data) -> {
        int multiplier = data.readInt();
        return new WorkbenchMenu(windowId, inv, ContainerLevelAccess.f_39287_, multiplier);
    }));
}
