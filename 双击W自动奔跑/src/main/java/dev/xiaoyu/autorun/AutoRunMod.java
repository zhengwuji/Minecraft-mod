package dev.xiaoyu.autorun;

import net.minecraft.client.Minecraft;
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
    public static boolean isDoubleWSprinting = false;
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

        // 检测前进键 (W)
        if (mc.options.keyUp.matches(event.getKey(), event.getScanCode())) {
            if (event.getAction() == GLFW.GLFW_PRESS) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastWPressTime < 300) {
                    // 双击 W，开启疾跑标记
                    isDoubleWSprinting = true;
                    lastWPressTime = 0;
                } else {
                    lastWPressTime = currentTime;
                }
            } else if (event.getAction() == GLFW.GLFW_RELEASE) {
                // 松开 W 键，立即关闭疾跑标记，恢复正常
                isDoubleWSprinting = false;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) {
                // 如果没有按住 W 键或打开了菜单，确保重置
                if (!mc.options.keyUp.isDown() || mc.screen != null) {
                    isDoubleWSprinting = false;
                }

                // 按住 W 且在双击状态下，保持强行疾跑
                if (isDoubleWSprinting && mc.options.keyUp.isDown()) {
                    if (!mc.player.isSprinting()) {
                        mc.player.setSprinting(true);
                    }
                }
            }
        }
    }
}