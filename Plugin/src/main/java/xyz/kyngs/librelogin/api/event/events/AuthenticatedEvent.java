package xyz.kyngs.librelogin.api.event.events;

import xyz.kyngs.librelogin.api.event.PlayerBasedEvent;

/**
 * This event is called after the player has authenticated
 * Note, that this event will both be called if the player has authenticated manually, or automatically.
 * Use {@link #getReason()} to check the reason for authentication.
 *
 * @author kyngs
 */
public interface AuthenticatedEvent<P, S> extends PlayerBasedEvent<P, S> {

    AuthenticationReason getReason();

    enum AuthenticationReason {
        LOGIN,
        REGISTER,
        PREMIUM,
        SESSION
    }

}
