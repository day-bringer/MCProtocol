package io.github.daybringer.packet.handle;

import io.github.daybringer.packet.IPacketRegister;
import net.minecraft.network.protocol.Packet;

import java.util.List;

public interface IPacketHandler<THandler> extends IPacketRegister<THandler>
{
    /**
     * Invokes all registered packet handlers for the given packet.
     *
     * @param packet the packet to process
     * @return {@code true} if any handler indicates the packet should be canceled; {@code false} otherwise
     */
    boolean callHandlers(final Packet<?> packet);
    List<THandler> getPacketHandlers(Class<? extends Packet<?>> packetType);
}
