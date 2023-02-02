package xyz.kyngs.librelogin.api.server;

/**
 * An interface which provides information obtained by pinging servers;
 *
 * @param <S>
 */
public interface ServerPinger<S> {

    /**
     * Gets the latest ping of the server, returns null if the server is not online.
     * Can block if the server has not yet been pinged.
     *
     * @param server The server to ping
     * @return The data of the server, or null if the server is not online
     */
    ServerPing getLatestPing(S server);

}
