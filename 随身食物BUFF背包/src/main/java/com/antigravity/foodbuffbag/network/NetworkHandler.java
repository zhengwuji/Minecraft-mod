package com.antigravity.foodbuffbag.network;

import com.antigravity.foodbuffbag.FoodBuffBag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("removal")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FoodBuffBag.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(
                id++,
                PacketOpenFoodBag.class,
                PacketOpenFoodBag::toBytes,
                PacketOpenFoodBag::new,
                PacketOpenFoodBag::handle
        );
        CHANNEL.registerMessage(
                id++,
                PacketChangePage.class,
                PacketChangePage::toBytes,
                PacketChangePage::new,
                PacketChangePage::handle
        );
    }
}
