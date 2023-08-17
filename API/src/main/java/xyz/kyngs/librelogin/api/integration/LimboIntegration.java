package xyz.kyngs.librelogin.api.integration;

/**
 * This interface provides support for Limbo.
 *
 * @author bivashy
 */
public interface LimboIntegration<S> {

    /**
     * Creates a limbo server.
     *
     * @param serverName The name of the limbo server to be created.
     * @return An instance of the created limbo server.
     */
    S createLimbo(String serverName);
}
