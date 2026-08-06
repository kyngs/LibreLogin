/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.integration.luckperms;

import net.luckperms.api.context.ContextCalculator;
import net.luckperms.api.context.ContextConsumer;
import net.luckperms.api.context.ContextSet;
import net.luckperms.api.context.ImmutableContextSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

public class AuthorizedContext<P> implements ContextCalculator<P> {

    private final AuthenticLibreLogin<P, ?> plugin;

    public AuthorizedContext(AuthenticLibreLogin<P, ?> plugin) {
        this.plugin = plugin;
    }

    private static final String KEY = "librelogin-authorized";

    @Override
    public void calculate(@NonNull P player, @NonNull ContextConsumer consumer) {
        consumer.accept(KEY, Boolean.toString(plugin.getAuthorizationProvider().isAuthorized(player)));
    }

    @Override
    public @NonNull ContextSet estimatePotentialContexts() {
        return ImmutableContextSet.builder()
                .add(KEY, "true")
                .add(KEY, "false")
                .build();
    }
}
