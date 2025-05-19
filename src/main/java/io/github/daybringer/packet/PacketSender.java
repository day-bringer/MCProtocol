package io.github.daybringer.packet;

import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Utility class for sending packets to players on a Minecraft server.
 * <p>
 * This class provides methods to send custom NMS packets to individual players
 * or broadcast them to all online players.
 * <p>
 * It uses CraftBukkit internals to access the underlying NMS {@link ServerPlayer}
 * and send packets through their {@link net.minecraft.server.network.ServerGamePacketListenerImpl}.
 */
public class PacketSender
{
    private static final PacketSender instance = new PacketSender();
    private PacketSender() {
        if(instance != null)
            throw new IllegalStateException("PacketSender already instantiated");
    }

    public void sendToPlayer(Player player, Packet<?> packet)
    {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        serverPlayer.connection.send(packet);
    }

    public void sendAll(Packet<?> packet)
    {
        Bukkit.getOnlinePlayers().forEach(player -> sendToPlayer(player, packet));
    }

    public static PacketSender get()
    {
        if(instance == null)
            throw new IllegalStateException("PacketSender not yet initialized");
        return instance;
    }
}
