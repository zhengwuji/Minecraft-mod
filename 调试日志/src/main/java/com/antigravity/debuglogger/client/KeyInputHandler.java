package com.antigravity.debuglogger.client;

import com.antigravity.debuglogger.DebugLogger;
import com.antigravity.debuglogger.util.LogCollector;
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

@Mod.EventBusSubscriber(modid = DebugLogger.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {

    public static final KeyMapping KEY_EXPORT_LOG = new KeyMapping(
            "key.debuglogger.export_log",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F9,
            "key.categories.debuglogger"
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
        event.register(KEY_EXPORT_LOG);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = getMinecraft();
        if (mc == null) return;
        Player player = getPlayer(mc);
        if (player == null || getScreen(mc) != null) return;

        if (KEY_EXPORT_LOG.consumeClick()) {
            LogCollector.exportDevReport(player);
        }
    }
}
