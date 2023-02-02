package xyz.kyngs.librelogin.common.image;

import xyz.kyngs.librelogin.api.image.ImageProjector;
import xyz.kyngs.librelogin.common.AuthenticHandler;
import xyz.kyngs.librelogin.common.AuthenticLibrePremium;

public abstract class AuthenticImageProjector<P, S> extends AuthenticHandler<P, S> implements ImageProjector<P> {

    public AuthenticImageProjector(AuthenticLibrePremium<P, S> plugin) {
        super(plugin);
    }

    public abstract void enable();

}
