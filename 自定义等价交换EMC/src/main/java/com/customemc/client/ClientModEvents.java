package com.customemc.client;

import com.customemc.CustomEMCMod;
import com.customemc.client.gui.CustomEMCScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = CustomEMCMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEvents {

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyInit.OPEN_GUI_KEY);
    }

    @Mod.EventBusSubscriber(modid = CustomEMCMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeClientEvents {

        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (KeyInit.OPEN_GUI_KEY.consumeClick()) {
                if (Minecraft.getInstance().screen == null) {
                    Minecraft.getInstance().setScreen(new CustomEMCScreen());
                }
            }
        }
    }
}
