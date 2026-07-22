package com.antigravity.debuglogger.client;

import com.antigravity.debuglogger.DebugLogger;
import com.antigravity.debuglogger.util.LogCollector;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DebugLogger.MOD_ID, value = Dist.CLIENT)
public class DebugOverlay {

    public static boolean enableOverlay = true;

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

    private static Font getFont(Minecraft mc) {
        try {
            return mc.font;
        } catch (NoSuchFieldError e) {
            try {
                return (Font) Minecraft.class.getField("f_91062_").get(mc);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    private static PoseStack getPoseStack(GuiGraphics graphics) {
        try {
            return graphics.pose();
        } catch (NoSuchMethodError e) {
            try {
                java.lang.reflect.Method m = GuiGraphics.class.getMethod("m_280168_");
                return (PoseStack) m.invoke(graphics);
            } catch (Throwable t) {
                return null;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        if (!enableOverlay) return;
        try {
            Minecraft mc = getMinecraft();
            if (mc == null || getScreen(mc) != null) return;

            Font font = getFont(mc);
            if (font == null) return;

            GuiGraphics graphics = event.getGuiGraphics();
            PoseStack poseStack = getPoseStack(graphics);

            if (poseStack != null) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 100);
            }

            String title = "§e[开发者调试日志 DevLogger] §f按 §aF9 §f导出诊断报告";
            String stats = "§7防护断言拦截计数: §c" + LogCollector.INTERCEPTED_CRASHED_ASSERTIONS.get();

            try {
                graphics.drawString(font, title, 10, 10, 0xFFFFFF, true);
                graphics.drawString(font, stats, 10, 22, 0xFFFFFF, true);
            } catch (Throwable ignored) {
            }

            if (poseStack != null) {
                poseStack.popPose();
            }
        } catch (Throwable ignored) {
        }
    }
}
