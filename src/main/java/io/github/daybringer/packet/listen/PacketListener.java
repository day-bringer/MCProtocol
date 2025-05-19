package io.github.daybringer.packet.listen;

import io.github.daybringer.Main;
import io.github.daybringer.packet.handle.IPacketHandler;
import io.github.daybringer.packet.handle.PacketHandler;
import io.github.daybringer.packet.handle.RegisteredPacketHandlerContainer;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

@ChannelHandler.Sharable
public final class PacketListener implements IPacketListener<RegisteredPacketHandlerContainer>
{
    private static final PacketListener INSTANCE = new PacketListener();

    private final PacketHandler packetHandler;
    private final HashMap<UUID, Connection> connections;
    private PacketListener()
    {
        if(INSTANCE != null)
            throw new IllegalStateException("PacketListener already initialized");

        packetHandler = new PacketHandler();
        connections = new HashMap<>();
    }
    @Override
    public void startListening(Player player) {
        try{
            if(isListening(player))
            {
                return;
            }
            Connection connection = getConnection(player);
            String handlerKey = player.getUniqueId().toString();

            connections.put(player.getUniqueId(), connection);
            connection.channel.pipeline().addBefore("packet_handler", handlerKey, this);
        }
        catch (IllegalStateException e)
        {
            Bukkit.getLogger().log(Level.WARNING, "Could not get connection object for player: " + player.getName(), e);
        }
    }

    @Override
    public void cancelListening(Player player)
    {
        if(!isListening(player))
            return;
        String handlerKey = player.getUniqueId().toString();
        Connection connection = connections.remove(player.getUniqueId());

        connection.channel.pipeline().remove(handlerKey);
    }

    public void cancel()
    {
        Bukkit.getOnlinePlayers().forEach(this::cancelListening);
    }

    @Override
    public boolean isListening(Player player) {
        return connections.containsKey(player.getUniqueId());
    }

    @Override
    public IPacketHandler<RegisteredPacketHandlerContainer> getHandler() {
        return packetHandler;
    }

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception
    {
        boolean cancelFlow = false;

        if(o instanceof Packet<?> packet)
        {
            if(Bukkit.isPrimaryThread())
            {
                cancelFlow = packetHandler.callHandlers(packet);
            }
            else
            {
                cancelFlow = Bukkit.getScheduler().callSyncMethod(Main.getPlugin(Main.class), ()->packetHandler.callHandlers(packet)).get();
            }
            if(cancelFlow)
            {
                if(Bukkit.isPrimaryThread())
                {
                    packetHandler.callCancelHandlers(packet);
                }
                else
                {
                    Bukkit.getScheduler().callSyncMethod(Main.getPlugin(Main.class), ()->packetHandler.callCancelHandlers(packet));
                }
            }
        }

        if(!cancelFlow) IPacketListener.super.channelRead(channelHandlerContext, o);
    }

    public static PacketListener get()
    {
        if(INSTANCE == null)
            throw new IllegalStateException("PacketListener not yet initialized");
        return INSTANCE;
    }
}
