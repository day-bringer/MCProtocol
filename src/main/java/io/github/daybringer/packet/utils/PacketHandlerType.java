package io.github.daybringer.packet.utils;

/**
 * Defines the type of packet handler.
 * <ul>
 *   <li>{@link #NORMAL} — a standard handler that processes packets without affecting their flow.</li>
 *   <li>{@link #CANCELABLE} — a handler that can cancel the packet processing by returning {@code true}.</li>
 * </ul>
 */
public enum PacketHandlerType
{
    NORMAL,
    CANCELABLE
}
