package io.github.daybringer.packet;

import io.github.daybringer.packet.listen.IPacketListener;
import io.github.daybringer.packet.listen.Listenable;
import io.github.daybringer.packet.utils.PacketComparable;

public interface IPacketRegister<THandler> extends PacketComparable
{
    /**
     * Registers a packet listener to receive and handle packets.
     *
     * @param listener the packet listener to register
     * @return {@code true} if the listener was successfully registered; {@code false} otherwise
     */
    boolean registerPacketListener(Listenable listener);


}
