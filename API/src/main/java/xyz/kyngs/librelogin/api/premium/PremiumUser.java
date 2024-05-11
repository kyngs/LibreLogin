/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.premium;

import java.util.UUID;

/**
 * This record holds basic premium information.
 *
 * @param uuid The (premium) UUID of the player.
 * @param name The name of the player.
 * @param reliable Whether the information is reliable. If not, the {@link PremiumUser#name()} string might not match the queried name (ignoring case).
 */
public record PremiumUser(UUID uuid, String name, boolean reliable) {

}
