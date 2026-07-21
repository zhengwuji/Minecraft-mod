package com.antigravity.quadhotbar.client;

import com.antigravity.quadhotbar.QuadHotbar;
import com.antigravity.quadhotbar.network.NetworkHandler;
import com.antigravity.quadhotbar.network.PacketSwitchHotbar;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = QuadHotbar.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    public static final KeyMapping KEY_NEXT_HOTBAR = new KeyMapping(
            "key.quadhotbar.next_row",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.quadhotbar"
    );

    public static final KeyMapping KEY_PREV_HOTBAR = new KeyMapping(
            "key.quadhotbar.prev_row",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "key.categories.quadhotbar"
    );

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_NEXT_HOTBAR);
        event.register(KEY_PREV_HOTBAR);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (KEY_NEXT_HOTBAR.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketSwitchHotbar(1));
        } else if (KEY_PREV_HOTBAR.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketSwitchHotbar(-1));
        }
    }
}
