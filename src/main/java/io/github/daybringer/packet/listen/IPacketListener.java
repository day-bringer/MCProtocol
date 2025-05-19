package io.github.daybringer.packet.listen;

import io.github.daybringer.packet.handle.IPacketHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import jline.internal.Nullable;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_21_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

/**
 * Represents a listener for handling incoming network packets associated with a specific player.
 * <p>
 * This interface extends Netty's {@link ChannelInboundHandler} to allow integration into the
 * Minecraft server's networking pipeline. Implementations are expected to register and manage
 * packet listeners on a per-player basis.
 */
public interface IPacketListener<THandler> extends ChannelInboundHandler {

    /**
     * Starts listening for packets from the specified player.
     * <p>
     * Implementations should attach the packet listener to the player’s network channel and begin
     * intercepting packets as needed.
     *
     * @param player the player to begin listening to
     */
    void startListening(Player player);

    /**
     * Cancels packet listening for the specified player.
     * <p>
     * This should remove any previously attached packet listeners from the player's network channel
     * and clean up any internal state.
     *
     * @param player the player to stop listening to
     */
    void cancelListening(Player player);

    /**
     * Checks whether this packet listener is currently active for the given player.
     *
     * @param player the player to check
     * @return {@code true} if listening to packets from the player; {@code false} otherwise
     */
    boolean isListening(Player player);

    IPacketHandler<THandler> getHandler();
    /**
     * Attempts to retrieve the network {@link Connection} object associated with the given player.
     * <p>
     * This method tries multiple known field names that may hold the connection reference,
     * to handle differences between Minecraft server versions or mappings.
     * <p>
     * It accesses the player's underlying {@link ServerGamePacketListenerImpl} via NMS (net.minecraft.server) internals,
     * reflecting on the {@link ServerCommonPacketListenerImpl} class fields to find the connection.
     * <p>
     * If none of the expected fields are found or accessible, this method throws an {@link IllegalStateException}.
     *
     * @param player the Bukkit player whose connection to retrieve
     * @return the player's {@link Connection} if found; {@code null} if no valid connection instance is present
     * @throws IllegalStateException if the connection field cannot be located or accessed via reflection
     */
    @Nullable
    default Connection getConnection(final Player player) throws IllegalStateException
    {
        String[] possibleFieldNames = {"e", "connection", "field_45013"};
        ServerPlayer playerServer = ((CraftPlayer) player).getHandle();
        ServerGamePacketListenerImpl listener = playerServer.connection;
        for(String fieldName : possibleFieldNames)
        {
            try {

                Field connectionField = ServerCommonPacketListenerImpl.class.getDeclaredField(fieldName);
                connectionField.setAccessible(true);
                Object connectionObject = connectionField.get(listener);

                if(connectionObject instanceof Connection connection)
                    return connection;
            }
            catch (NoSuchFieldException | IllegalAccessException e) {
                continue;
            }
        }
        throw new IllegalStateException("Failed to find connection field in listener.");

    }

    //Defaults not important to packet reading
    default void channelRegistered(ChannelHandlerContext var1) throws Exception {
        var1.fireChannelRegistered();
    }

    default void channelUnregistered(ChannelHandlerContext var1) throws Exception {
        var1.fireChannelUnregistered();
    }

    default void channelActive(ChannelHandlerContext var1) throws Exception {
        var1.fireChannelActive();
    }

    default void channelInactive(ChannelHandlerContext var1) throws Exception {
        var1.fireChannelInactive();
    }


    default void channelReadComplete(ChannelHandlerContext var1) throws Exception {
        var1.fireChannelReadComplete();
    }

    default void userEventTriggered(ChannelHandlerContext var1, Object var2) throws Exception
    {
        var1.fireUserEventTriggered(var2);
    }

    default void channelWritabilityChanged(ChannelHandlerContext var1) throws Exception
    {
        var1.fireChannelWritabilityChanged();
    }

    default void exceptionCaught(ChannelHandlerContext var1, Throwable var2) throws Exception
    {
        var1.fireExceptionCaught(var2);
    }
    default void handlerAdded(ChannelHandlerContext var1) throws Exception
    {

    }

    default void handlerRemoved(ChannelHandlerContext var1) throws Exception
    {

    }

    default void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ctx.fireChannelRead(msg);
    }
}
