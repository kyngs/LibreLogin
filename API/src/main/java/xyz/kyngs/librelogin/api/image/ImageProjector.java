/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.image;

import java.awt.image.BufferedImage;

/**
 * This interface is used to project images to the players
 *
 * @param <P> The type of the player
 */
public interface ImageProjector<P> {

    /**
     * This method projects an image to the player
     *
     * @param player The player to project the image to
     * @param image  The image to project
     */
    void project(BufferedImage image, P player);

    /**
     * This method allows the projector to decide, whether the player can have an image projected to them
     *
     * @param player The player to check
     * @return Whether the player can have an image projected to them
     */
    boolean canProject(P player);

}
