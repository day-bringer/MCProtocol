package io.github.daybringer.packet.utils;

import net.minecraft.network.protocol.Packet;

public interface PacketComparable
{
    default boolean isServerboundPacket(Class<? extends Packet<?>> inboundPacket)
    {
        String className = inboundPacket.getSimpleName();

        return !className.startsWith("Clientbound");
    }
}
