package com.tfar.anviltweaks.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class Message {
    private static final String PROTOCOL_VERSION = "1";
    public static SimpleChannel INSTANCE;

    @SuppressWarnings("removal")
    public static void registerMessages(String channelName) {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(channelName, "main"),
                () -> PROTOCOL_VERSION,
                PROTOCOL_VERSION::equals,
                PROTOCOL_VERSION::equals
        );

        int id = 0;
        INSTANCE.registerMessage(
                id++,
                CPacketAnvilRename.class,
                CPacketAnvilRename::encode,
                CPacketAnvilRename::decode,
                CPacketAnvilRename::handle
        );
    }
}
