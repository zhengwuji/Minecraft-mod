package com.antigravity.devhelper.network;

import com.antigravity.devhelper.DevHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class DevHelperNetwork {
    private static final String PROTOCOL_VERSION = "1.0.0";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(DevHelper.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        CHANNEL.registerMessage(
                packetId++,
                C2SUpdateAttributePacket.class,
                C2SUpdateAttributePacket::encode,
                C2SUpdateAttributePacket::new,
                C2SUpdateAttributePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                packetId++,
                C2SUpdatePlayerStatsPacket.class,
                C2SUpdatePlayerStatsPacket::encode,
                C2SUpdatePlayerStatsPacket::new,
                C2SUpdatePlayerStatsPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }
}
