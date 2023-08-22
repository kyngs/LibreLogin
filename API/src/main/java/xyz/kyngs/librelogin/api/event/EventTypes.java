/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.event;

import xyz.kyngs.librelogin.api.event.events.*;

/**
 * This class stores all available event types.
 *
 * @param <P> The player type
 * @param <S> The server type
 */
public class EventTypes<P, S> {

    /**
     * Represents an authenticated event type.
     */
    public EventType<P, S, AuthenticatedEvent<P, S>> authenticated = new EventType<>(AuthenticatedEvent.class);
    /**
     * Represents a limbo server choose event type.
     */
    public EventType<P, S, LimboServerChooseEvent<P, S>> limboServerChoose = new EventType<>(LimboServerChooseEvent.class);
    /**
     * Represents a lobby server choose event type.
     */
    public EventType<P, S, LobbyServerChooseEvent<P, S>> lobbyServerChoose = new EventType<>(LobbyServerChooseEvent.class);
    /**
     * Represents a password change event type.
     */
    public EventType<P, S, PasswordChangeEvent<P, S>> passwordChange = new EventType<>(PasswordChangeEvent.class);
    /**
     * Represents a premium login switch event type.
     */
    public EventType<P, S, PremiumLoginSwitchEvent<P, S>> premiumLoginSwitch = new EventType<>(PremiumLoginSwitchEvent.class);

    /**
     * Prevents instantiation from outside.
     */
    EventTypes() {
    }

}
