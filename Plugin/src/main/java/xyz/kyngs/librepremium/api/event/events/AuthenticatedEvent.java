package xyz.kyngs.librepremium.api.event.events;

import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.PlayerBasedEvent;

/**
 * This event is called after the player has authenticated
 * Note, that this event will both be called if the player has authenticated manually, or automatically.
 * Use {@link User#autoLoginEnabled()} to check whether the player has authenticated automatically
 *
 * @author kyngs
 */
public interface AuthenticatedEvent extends PlayerBasedEvent {
}
