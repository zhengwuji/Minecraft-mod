package dev.xiaoyu.autorun;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.lwjgl.glfw.GLFW;

@Mod("autorun")
public class AutoRunMod {
    public static boolean autoRunActive = false;
    private static long lastWPressTime = 0;

    public AutoRunMod() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        // Check if key corresponds to the forward key mapping
        if (mc.options.keyUp.matches(event.getKey(), event.getScanCode())) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastWPressTime < 300) {
                    // Double clicked W!
                    autoRunActive = !autoRunActive;
                    if (autoRunActive) {
                        mc.player.displayClientMessage(Component.literal("\u00a7a[\u81ea\u52a8\u5954\u8dd1] \u5df2\u5f00\u542f"), true);
                    } else {
                        mc.player.displayClientMessage(Component.literal("\u00a7c[\u81ea\u52a8\u5954\u8dd1] \u5df2\u5173\u95ed"), true);
                        // Reset key state
                        mc.options.keyUp.setDown(false);
                    }
                    lastWPressTime = 0; // Prevent triple click issues
                    return; // EXIT EARLY to prevent immediate self-cancellation!
                }
                lastWPressTime = currentTime;
            }
        }

        // If auto run is active, any other movement key press cancels it
        if (autoRunActive && event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.options.keyDown.matches(event.getKey(), event.getScanCode()) ||
                mc.options.keyLeft.matches(event.getKey(), event.getScanCode()) ||
                mc.options.keyRight.matches(event.getKey(), event.getScanCode()) ||
                mc.options.keyUp.matches(event.getKey(), event.getScanCode())) {
                
                autoRunActive = false;
                mc.options.keyUp.setDown(false);
                mc.player.displayClientMessage(Component.literal("\u00a7c[\u81ea\u52a8\u5954\u8dd1] \u5df2\u5173\u95ed"), true);
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                if (mc.screen != null && autoRunActive) {
                    autoRunActive = false;
                    mc.options.keyUp.setDown(false);
                }
                
                if (autoRunActive) {
                    // Keep W key down
                    mc.options.keyUp.setDown(true);
                    
                    // Force sprinting
                    if (!mc.player.isSprinting() && mc.player.moveDist > 0) {
                        mc.player.setSprinting(true);
                    }
                }
            }
        }
    }
}