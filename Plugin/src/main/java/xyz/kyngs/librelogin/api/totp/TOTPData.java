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
