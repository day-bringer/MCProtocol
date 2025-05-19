package io.github.daybringer.packet.handle;

import net.minecraft.network.protocol.Packet;

import java.util.List;

public interface IPacketCancelHandler<TCancelHandler>
{
    boolean callCancelHandlers(final Packet<?> packet);
    List<TCancelHandler> getPacketCancelHandlers(Class<? extends Packet<?>> packetType);
}
