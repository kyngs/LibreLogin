package xyz.kyngs.librepremium.paper;

import xyz.kyngs.librepremium.paper.protocollib.ClientPublicKey;

public record EncryptionData(String username, byte[] token, ClientPublicKey publicKey) {
}
