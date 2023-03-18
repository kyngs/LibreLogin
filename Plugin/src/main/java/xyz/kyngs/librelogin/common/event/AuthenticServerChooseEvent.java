/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.event;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.ServerChooseEvent;

public class AuthenticServerChooseEvent<P, S> extends AuthenticPlayerBasedEvent<P, S> implements ServerChooseEvent<P, S> {

    private S server = null;

    public AuthenticServerChooseEvent(@Nullable User user, P player, LibreLoginPlugin<P, S> plugin) {
        super(user, player, plugin);
    }

    @Nullable
    @Override
    public S getServer() {
        return server;
    }

    @Override
    public void setServer(S server) {
        this.server = server;
    }
}
