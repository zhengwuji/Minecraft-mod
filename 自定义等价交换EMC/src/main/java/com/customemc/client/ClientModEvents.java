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
            try {
                if (KeyInit.OPEN_GUI_KEY != null && KeyInit.OPEN_GUI_KEY.getKey() != null) {
                    if (event.getKey() == KeyInit.OPEN_GUI_KEY.getKey().getValue() && event.getAction() == 1) { // 1 == GLFW_PRESS
                        Minecraft mc = Minecraft.getInstance();
                        if (mc.screen == null) {
                            mc.setScreen(new CustomEMCScreen());
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
