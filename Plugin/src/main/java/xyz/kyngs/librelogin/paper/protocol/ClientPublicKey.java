/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.paper.protocol;

import com.github.retrooper.packetevents.util.crypto.SignatureData;

import java.security.PublicKey;
import java.time.Instant;

public record ClientPublicKey(Instant expire, PublicKey key, byte[] signature) {

    public boolean expired(Instant timestamp) {
        return !timestamp.isBefore(expire);
    }

    public SignatureData toSignatureData() {
        return new SignatureData(expire, key, signature);
    }
}
