/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common;

import xyz.kyngs.librelogin.api.PlatformHandle;

public class AuthenticHandler<P, S> {

    protected final AuthenticLibreLogin<P, S> plugin;
    protected final PlatformHandle<P, S> platformHandle;

    public AuthenticHandler(AuthenticLibreLogin<P, S> plugin) {
        this.plugin = plugin;
        this.platformHandle = plugin.getPlatformHandle();
    }
}
