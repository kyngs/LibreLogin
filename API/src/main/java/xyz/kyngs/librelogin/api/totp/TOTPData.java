/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.totp;

import java.awt.image.BufferedImage;

/**
 * A basic record to hold TOTP data.
 *
 * @param qr     The QR code of the TOTP.
 * @param secret The secret of the TOTP.
 */
public record TOTPData(BufferedImage qr, String secret) {

}
