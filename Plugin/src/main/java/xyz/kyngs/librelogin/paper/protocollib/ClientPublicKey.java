/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper.protocollib;

import java.security.PublicKey;
import java.time.Instant;

public class ClientPublicKey {
    private final Instant expire;
    private final PublicKey key;
    private final byte[] signature;

    public ClientPublicKey(Instant expire, PublicKey key, byte[] signature) {
        this.expire = expire;
        this.key = key;
        this.signature = signature;
    }

    public Instant getExpire() {
        return expire;
    }

    public PublicKey getKey() {
        return key;
    }

    public byte[] getSignature() {
        return signature;
    }

    public boolean expired(Instant timestamp) {
        return !timestamp.isBefore(expire);
    }
}
