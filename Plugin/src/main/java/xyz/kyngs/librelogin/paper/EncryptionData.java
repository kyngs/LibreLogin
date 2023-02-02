package xyz.kyngs.librelogin.paper;

import xyz.kyngs.librelogin.paper.protocollib.ClientPublicKey;

public record EncryptionData(String username, byte[] token, ClientPublicKey publicKey) {
}
