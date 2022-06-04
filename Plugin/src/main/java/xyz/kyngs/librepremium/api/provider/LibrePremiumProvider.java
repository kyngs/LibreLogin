package xyz.kyngs.librepremium.api.provider;

import xyz.kyngs.librepremium.api.LibrePremiumPlugin;

/**
 * This class is used to obtain the instance of the plugin
 *
 * @param <P> The type of the player
 * @param <S> The type of the server
 */
public interface LibrePremiumProvider<P, S> {

    LibrePremiumPlugin<P, S> getLibrePremium();

}
