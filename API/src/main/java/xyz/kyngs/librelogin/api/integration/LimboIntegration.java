/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.integration;

/**
 * This interface provides support for Limbo.
 *
 * @author bivashy
 */
public interface LimboIntegration<S> {

    /**
     * Creates a limbo server.
     *
     * @param serverName The name of the limbo server to be created.
     * @return An instance of the created limbo server.
     */
    S createLimbo(String serverName);
}
