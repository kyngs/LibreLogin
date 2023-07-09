/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.event.events;

import org.jetbrains.annotations.Nullable;
import xyz.kyngs.librelogin.api.LibreLoginPlugin;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.LobbyServerChooseEvent;
import xyz.kyngs.librelogin.common.event.AuthenticServerChooseEvent;

public class AuthenticLobbyServerChooseEvent<P, S> extends AuthenticServerChooseEvent<P, S> implements LobbyServerChooseEvent<P, S> {

    private final boolean fallback;
    private boolean cancelled = false;

    public AuthenticLobbyServerChooseEvent(@Nullable User user, P player, LibreLoginPlugin<P, S> plugin, boolean fallback) {
        super(user, player, plugin);

        this.fallback = fallback;
    }

    @Override
    public boolean isFallback() {
        return fallback;
    }


    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
}
