package xyz.kyngs.librepremium.api.totp;

import java.awt.image.BufferedImage;

public record TOTPData(BufferedImage qr, String secret) {

}
