package xyz.kyngs.librepremium.api.provider;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

public interface LibrePremiumProvider<P, S> {

    LibrePremiumPlugin<P, S> getLibrePremium();

}
