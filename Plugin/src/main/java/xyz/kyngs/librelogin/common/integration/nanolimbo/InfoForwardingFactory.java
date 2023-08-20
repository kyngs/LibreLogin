/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package xyz.kyngs.librelogin.common.integration.nanolimbo;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import ua.nanit.limbo.server.data.InfoForwarding;

// Class for hiding InfoForwarding dirty way initialization
public class InfoForwardingFactory {

    public InfoForwarding none() {
        Map<String, Object> map = Map.of("type", "NONE");
        return createForwarding(map);
    }

    public InfoForwarding legacy() {
        Map<String, Object> map = Map.of("type", "LEGACY");
        return createForwarding(map);
    }

    public InfoForwarding modern(byte[] secretKey) {
        String rawKey = new String(secretKey, StandardCharsets.UTF_8);
        Map<String, Object> map = Map.of(
                "type", "MODERN",
                "secret", rawKey
        );
        return createForwarding(map);
    }

    public InfoForwarding bungeeGuard(Collection<String> tokens) {
        Map<String, Object> map = Map.of(
                "type", "MODERN",
                "tokens", new ArrayList<>(tokens)
        );
        return createForwarding(map);
    }

    private InfoForwarding createForwarding(Map<String, Object> map) {
        InfoForwarding forwarding = new InfoForwarding();
        for (Entry<String, Object> entry : map.entrySet()) {
            Class<?> clazz = forwarding.getClass();
            try {
                Field field = clazz.getField(entry.getKey());
                field.setAccessible(true);
                field.set(forwarding, entry.getValue());
            } catch (NoSuchFieldException | IllegalAccessException ignored) {
            }
        }
        return forwarding;
    }


}
