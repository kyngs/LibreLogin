package xyz.kyngs.librepremium.common.image;

import xyz.kyngs.librepremium.api.image.ImageProjector;
import xyz.kyngs.librepremium.common.AuthenticHandler;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;

public abstract class AuthenticImageProjector<P, S> extends AuthenticHandler<P, S> implements ImageProjector<P> {

    public AuthenticImageProjector(AuthenticLibrePremium<P, S> plugin) {
        super(plugin);
    }

    public abstract void enable();

}
