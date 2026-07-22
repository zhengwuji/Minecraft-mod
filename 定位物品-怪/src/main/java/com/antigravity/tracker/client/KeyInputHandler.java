package com.antigravity.tracker.client;

import com.antigravity.tracker.ItemEntityTracker;
import com.antigravity.tracker.client.gui.TrackerScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ItemEntityTracker.MODID, value = Dist.CLIENT)
public class KeyInputHandler {

    public static final KeyMapping KEY_OPEN_TRACKER = new KeyMapping(
            "key.itementitytracker.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            "key.categories.itementitytracker"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_OPEN_TRACKER);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (KEY_OPEN_TRACKER.consumeClick()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.screen == null) {
                    mc.setScreen(new TrackerScreen());
                }
            }
        }
    }
}
