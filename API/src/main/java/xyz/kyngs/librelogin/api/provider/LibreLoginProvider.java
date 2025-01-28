/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.provider;

import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.util.SemanticVersion;

/**
 * This class is used to obtain the instance of the plugin
 *
 * @param <P> The type of the player
 * @param <S> The type of the server
 */
public interface LibreLoginProvider<P, S> {

    /**
     * Gets the instance of the plugin
     *
     * @return the instance of the plugin
     */
    LibreLoginPlugin<P, S> getLibreLogin();

    /**
     * Gets the plugin's version.
     *
     * @return The version
     * @since 0.25.0
     */
    String getVersion();

    /**
     * Gets the plugin's parsed version.
     *
     * @return The parsed version
     * @since 0.25.0
     */
    SemanticVersion getParsedVersion();

}
