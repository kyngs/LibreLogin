package xyz.kyngs.librelogin.api.provider;

import xyz.kyngs.librelogin.api.LibreLoginPlugin;

/**
 * This class is used to obtain the instance of the plugin
 *
 * @param <P> The type of the player
 * @param <S> The type of the server
 */
public interface LibreLoginProvider<P, S> {

    LibreLoginPlugin<P, S> getLibreLogin();

}
