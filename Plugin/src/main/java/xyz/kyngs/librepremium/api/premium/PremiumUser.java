package xyz.kyngs.librepremium.api.premium;

import java.util.UUID;

/**
 * This record holds basic premium information.
 *
 * @param uuid The (premium) UUID of the player.
 * @param name The name of the player.
 */
public record PremiumUser(UUID uuid, String name) {

}
