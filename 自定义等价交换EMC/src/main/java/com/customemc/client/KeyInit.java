package com.customemc.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

public class KeyInit {
    public static final String KEY_CATEGORY = "自定义等价交换EMC";

    public static final KeyMapping OPEN_GUI_KEY = new KeyMapping(
            "key.custom_emc.open_gui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            KEY_CATEGORY
    );
}
