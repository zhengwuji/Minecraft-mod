package com.antigravity.quadhotbar.network;

import com.antigravity.quadhotbar.QuadHotbar;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("removal")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(QuadHotbar.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                PacketSwitchHotbar.class,
                PacketSwitchHotbar::toBytes,
                PacketSwitchHotbar::new,
                PacketSwitchHotbar::handle
        );
    }
}
