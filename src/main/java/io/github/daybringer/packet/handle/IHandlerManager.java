package io.github.daybringer.packet.handle;


/**
 * Represents a manager responsible for maintaining and updating packet handlers.
 *
 * @param <TKey>    The type used as the key to identify handlers.
 * @param <THandler> The type of handler managed by this interface.
 */
public interface IHandlerManager<TKey, THandler>
{
    /**
     * Retrieves the handler associated with the given key.
     *
     * @param key The key for which to retrieve the handler.
     * @return The associated {@link IPacketHandler}, or {@code null} if none is found.
     */
    IPacketHandler<THandler> getHandler(TKey key);

    /**
     * Registers or replaces a handler for the specified key.
     *
     * @param key     The key for which the handler is being set.
     * @param handler The handler to associate with the key.
     */
    void setHandler(TKey key, IPacketHandler<THandler> handler);

    /**
     * Removes the handler associated with the specified key, if present.
     *
     * @param key The key whose handler should be removed.
     */
    void removeHandler(TKey key);

    /**
     * Clears all registered handlers from the manager.
     */
    void clearHandlers();

    /**
     * Updates the handler associated with the given key.
     * <p>
     * This can be used to refresh or reconfigure the existing handler mapping for a specific key.
     * </p>
     *
     * @param key     The key whose handler should be updated.
     * @param handler The updated {@link IPacketHandler} instance.
     */
    void updateHandlers(TKey key, IPacketHandler<THandler> handler);

}
