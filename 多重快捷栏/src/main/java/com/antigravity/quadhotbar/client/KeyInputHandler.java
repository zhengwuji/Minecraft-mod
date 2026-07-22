package com.antigravity.quadhotbar.client;

import com.antigravity.quadhotbar.QuadHotbar;
import com.antigravity.quadhotbar.network.NetworkHandler;
import com.antigravity.quadhotbar.network.PacketSwitchHotbar;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
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

    private static Minecraft getMinecraft() {
        try {
            return Minecraft.getInstance();
        } catch (NoSuchMethodError e) {
            try {
                java.lang.reflect.Method m = Minecraft.class.getMethod("m_91087_");
                return (Minecraft) m.invoke(null);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    private static Screen getScreen(Minecraft mc) {
        try {
            return mc.screen;
        } catch (NoSuchFieldError e) {
            try {
                return (Screen) Minecraft.class.getField("f_91074_").get(mc);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    private static Player getPlayer(Minecraft mc) {
        try {
            return mc.player;
        } catch (NoSuchFieldError e) {
            try {
                return (Player) Minecraft.class.getField("f_91073_").get(mc);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KEY_NEXT_HOTBAR);
        event.register(KEY_PREV_HOTBAR);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = getMinecraft();
        if (mc == null) return;
        if (getPlayer(mc) == null || getScreen(mc) != null) return;

        if (KEY_NEXT_HOTBAR.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketSwitchHotbar(1));
        } else if (KEY_PREV_HOTBAR.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketSwitchHotbar(-1));
        }
    }
}
