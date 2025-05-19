package io.github.daybringer.packet.handle;

import io.github.daybringer.packet.annotations.PacketCancelHandler;
import io.github.daybringer.packet.listen.IPacketListener;
import io.github.daybringer.packet.utils.PacketHandlerType;
import net.minecraft.network.protocol.Packet;
import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
/**
 * Manages registration and invocation of packet handlers for server-bound packets.
 * <p>
 * Supports two types of handlers:
 * <ul>
 *   <li>{@link PacketHandlerType#NORMAL}: Handlers that process packets without cancellation.</li>
 *   <li>{@link PacketHandlerType#CANCELABLE}: Handlers that can cancel packets by returning {@code true}.</li>
 * </ul>
 * <p>
 * Registered handlers are invoked when packets arrive. Handlers must be annotated
 * with {@link io.github.daybringer.packet.annotations.PacketHandler} and comply with the
 * expected method signatures and return types.
 */
public class PacketHandler implements IPacketHandler<RegisteredPacketHandlerContainer>, IPacketCancelHandler<RegisteredPacketHandlerContainer>
{
    private final HashMap<Class<? extends Packet<?>>, List<RegisteredPacketHandlerContainer>> packetHandlers;
    private final HashMap<Class<? extends Packet<?>>, List<RegisteredPacketHandlerContainer>> packetCancelHandlers;
    /**
     * Creates a new PacketHandler with empty handler registries.
     */
    public PacketHandler()
    {
        packetHandlers = new HashMap<>();
        packetCancelHandlers = new HashMap<>();
    }
    /**
     * Adds a normal packet handler for the given packet class.
     *
     * @param packetClass the class of packet to handle
     * @param handler the handler to add
     */
    private void addHandler(Class<? extends Packet<?>> packetClass, RegisteredPacketHandlerContainer handler)
    {
        packetHandlers.compute(packetClass, (k, v) -> (v == null) ? new ArrayList<>() : v).add(handler);
    }

    private void addCancelHandler(Class<? extends Packet<?>> packetClass, RegisteredPacketHandlerContainer handler)
    {
        packetCancelHandlers.compute(packetClass, (k, v) -> (v == null) ? new ArrayList<>() : v).add(handler);
    }

    /**
     * Registers all valid packet handler methods from the given listener instance.
     * <p>
     * Only methods annotated with {@link io.github.daybringer.packet.annotations.PacketHandler} are considered.
     * <p>
     * For handlers marked as cancelable, the method must return a boolean.
     * Methods must take exactly one parameter extending {@link Packet} and represent a server-bound packet.
     *
     * @param listener the packet listener instance containing handler methods
     * @return {@code true} if registration succeeded (currently always returns {@code false})
     */
    @Override
    public boolean registerPacketListener(IPacketListener<RegisteredPacketHandlerContainer> listener)
    {
        for(Method method : listener.getClass().getDeclaredMethods())
        {
            //Does class contain the PacketHandler annotation
            if(!method.isAnnotationPresent(io.github.daybringer.packet.annotations.PacketHandler.class) && !method.isAnnotationPresent(PacketCancelHandler.class))
                continue;
            var params = method.getParameterTypes();

            if(method.isAnnotationPresent(io.github.daybringer.packet.annotations.PacketHandler.class))
            {
                var annotation  = method.getAnnotation(io.github.daybringer.packet.annotations.PacketHandler.class);


                //If marked as cancel handler verify the return type
                if(annotation.handlerType() == PacketHandlerType.CANCELABLE)
                {
                    if(method.getReturnType() != boolean.class && method.getReturnType() != Boolean.class)
                    {
                        Bukkit.getLogger().log(Level.WARNING, "Packet Handler: "+ method.getClass().getSimpleName() + " : " + method.getName() + "with handler type CANCELABLE must return a boolean.");
                        continue;
                    }
                }
            }
            //Does method only contain one argument and is of type Packet
            if(params.length != 1 || !Packet.class.isAssignableFrom(params[0]))
                continue;

            @SuppressWarnings("unchecked")
            Class<? extends Packet<?>> packetType = (Class<? extends Packet<?>>) params[0];

            //Is a server bound packet
            if(!isServerboundPacket(packetType))
            {
                continue;
            }

            if(method.isAnnotationPresent(io.github.daybringer.packet.annotations.PacketCancelHandler.class))
            {
                addCancelHandler(packetType, new RegisteredPacketHandlerContainer(listener, method));
            }
            else if(method.isAnnotationPresent(io.github.daybringer.packet.annotations.PacketHandler.class))
                addHandler(packetType, new RegisteredPacketHandlerContainer(listener, method));
        }
        return false;
    }

    /**
     * Retrieves the list of normal handlers registered for the specified packet type.
     *
     * @param packetType the class of packet
     * @return a list of registered normal handlers, or an empty list if none exist
     */
    public List<RegisteredPacketHandlerContainer> getPacketHandlers(Class<? extends Packet<?>> packetType)
    {
        if(!packetHandlers.containsKey(packetType))
            return new ArrayList<>();

        var handlers = packetHandlers.get(packetType);

        //Make sure handlers are not null;
        handlers = handlers == null ? new ArrayList<>() : handlers;

        return handlers;
    }

    /**
     * Calls all registered normal packet handlers for the given packet.
     * <p>
     * This method must be called on the main server thread.
     * If any handler returns a {@code Boolean}, that value is immediately returned,
     * which may indicate cancellation of the packet.
     * Exceptions thrown by handlers are caught and logged.
     *
     * @param packet the packet to process
     * @return {@code true} if a handler returned {@code true} (packet canceled), {@code false} otherwise
     */
    @Override
    public boolean callHandlers(Packet<?> packet)
    {
        boolean onMainThread = Bukkit.isPrimaryThread();

        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) packet.getClass();

        for(RegisteredPacketHandlerContainer handler : getPacketHandlers(packetClass))
        {
            if(onMainThread)
            {
                try {

                    Object obj = handler.handle().invoke(handler.instance(), packet);
                    //configure if handle has returning type
                    if(obj instanceof Boolean bool)
                        return bool;

                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error handling packet " + packetClass, e);
                }

            }
            else
            {
                Bukkit.getLogger().log(Level.WARNING, "Packet handler must be called on the main thread.");
            }
        }

        return false;
    }


    @Override
    public boolean callCancelHandlers(Packet<?> packet) {
        boolean onMainThread = Bukkit.isPrimaryThread();

        @SuppressWarnings("unchecked")
        Class<? extends Packet<?>> packetClass = (Class<? extends Packet<?>>) packet.getClass();

        for(RegisteredPacketHandlerContainer handler : getPacketCancelHandlers(packetClass))
        {
            if(onMainThread)
            {
                try {

                    handler.handle().invoke(handler.instance(), packet);
                    //configure if handle has returning type
                    return true;

                } catch (Exception e) {
                    Bukkit.getLogger().log(Level.WARNING, "Error handling packet " + packetClass, e);
                }

            }
            else
            {
                Bukkit.getLogger().log(Level.WARNING, "Packet handler must be called on the main thread.");
            }
        }

        return false;
    }

    @Override
    public List<RegisteredPacketHandlerContainer> getPacketCancelHandlers(Class<? extends Packet<?>> packetType) {
        if(!packetCancelHandlers.containsKey(packetType))
            return new ArrayList<>();

        var handlers = packetCancelHandlers.get(packetType);

        //Make sure handlers are not null;
        handlers = handlers == null ? new ArrayList<>() : handlers;

        return handlers;
    }
}
