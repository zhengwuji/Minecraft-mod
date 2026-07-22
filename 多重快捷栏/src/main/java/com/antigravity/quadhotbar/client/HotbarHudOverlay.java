package com.antigravity.quadhotbar.client;

import com.antigravity.quadhotbar.QuadHotbar;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuadHotbar.MOD_ID, value = Dist.CLIENT)
public class HotbarHudOverlay {

    /**
     * 根据用户需求：无需绘制第二行快捷栏，仅使用 V/B 键轮换原版单行快捷栏。
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        // 禁用第二行额外快捷栏 HUD 绘制
    }
}
