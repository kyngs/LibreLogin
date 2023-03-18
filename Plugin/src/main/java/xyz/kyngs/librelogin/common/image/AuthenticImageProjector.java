/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.image;

import xyz.kyngs.librelogin.api.image.ImageProjector;
import xyz.kyngs.librelogin.common.AuthenticHandler;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;

public abstract class AuthenticImageProjector<P, S> extends AuthenticHandler<P, S> implements ImageProjector<P> {

    public AuthenticImageProjector(AuthenticLibreLogin<P, S> plugin) {
        super(plugin);
    }

    public abstract void enable();

}
