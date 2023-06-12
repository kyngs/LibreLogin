/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event;

import java.util.function.Consumer;

/**
 * This interface manages events.
 *
 * @param <P> The player type
 * @param <S> The server type
 * @author kyngs
 */
public interface EventProvider<P, S> {

    /**
     * Returns the event types.
     *
     * @return The event types
     */
    default EventTypes<P, S> getTypes() {
        return new EventTypes<>();
    }

    /**
     * Allows you to subscribe to an event.
     *
     * @param type    The type of the event see {@link #getTypes()}
     * @param handler The handler to call when the event is fired
     * @param <E>     The event type
     */
    <E extends Event<P, S>> void subscribe(EventType<P, S, E> type, Consumer<E> handler);

    /**
     * Allows you to unsubscribe from an event.
     *
     * @param handler The handler to unsubscribe. Must be the same instance as your handler used in {@link #subscribe(EventType, Consumer)}
     */
    void unsubscribe(Consumer<? extends Event<P, S>> handler);

    /**
     * Allows you to fire an event.
     *
     * @param type  The type of the event see {@link #getTypes()}
     * @param event The event to fire
     * @param <E>   The event type
     */
    <E extends Event<P, S>> void fire(EventType<P, S, E> type, E event);

}
