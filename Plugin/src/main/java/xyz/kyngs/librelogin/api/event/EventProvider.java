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
     * Allows you to subscribe to an event.
     *
     * @param clazz   The class of the event
     * @param handler The handler to call when the event is fired
     * @param <E>     The event type
     */
    <E extends Event<P, S>> void subscribe(Class<? extends E> clazz, Consumer<E> handler);

    /**
     * Allows you to fire an event.
     *
     * @param clazz The class of the event
     * @param event The event to fire
     * @param <C>   The event type
     * @param <E>   The event type
     */
    <C extends Event<?, ?>, E extends C> void fire(Class<C> clazz, E event);

}
