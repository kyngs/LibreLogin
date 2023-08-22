/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.server;

/**
 * A record representing a server ping.
 * A server ping consists of the maximum number of players.
 *
 * @param maxPlayers The maximum number of players.
 */
public record ServerPing(int maxPlayers) {
}
