/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event;

import java.util.Objects;

/**
 * This class represents an event type.
 *
 * @param <P> The player type
 * @param <S> The server type
 * @param <E> The event type
 */
public class EventType<P, S, E extends Event<P, S>> {

    private final Class<?> clazz;

    EventType(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * Returns the event class.
     *
     * @return The event class
     */
    public Class<?> getClazz() {
        return clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventType<?, ?, ?> eventType = (EventType<?, ?, ?>) o;
        return Objects.equals(clazz, eventType.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clazz);
    }
}
