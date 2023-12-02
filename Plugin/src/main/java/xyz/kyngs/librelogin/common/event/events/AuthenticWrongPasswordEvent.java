/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.event.events;

import org.jetbrains.annotations.Nullable;

import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.WrongPasswordEvent;
import xyz.kyngs.librelogin.common.event.AuthenticPlayerBasedEvent;

public class AuthenticWrongPasswordEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements WrongPasswordEvent<P, S> {

    private final AuthenticationSource source;

    public AuthenticWrongPasswordEvent(@Nullable User user, P player, LibreLoginPlugin<P, S> plugin, AuthenticationSource source) {
        super(user, player, plugin);
        this.source = source;
    }

    @Override
    public AuthenticationSource getSource() {
        return source;
    }

}
