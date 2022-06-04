package xyz.kyngs.librepremium.api;

import xyz.kyngs.librepremium.api.authorization.AuthorizationProvider;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.database.ReadDatabaseProvider;
import xyz.kyngs.librepremium.api.database.ReadWriteDatabaseProvider;
import xyz.kyngs.librepremium.api.database.WriteDatabaseProvider;
import xyz.kyngs.librepremium.api.event.EventProvider;
import xyz.kyngs.librepremium.api.image.ImageProjector;
import xyz.kyngs.librepremium.api.premium.PremiumProvider;
import xyz.kyngs.librepremium.api.totp.TOTPProvider;
import xyz.kyngs.librepremium.api.util.SemanticVersion;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

/**
 * The main plugin interface.
 *
 * @param <P> The type of the player
 * @param <S> The type of the server
 */
public interface LibrePremiumPlugin<P, S> {

    /**
     * Gets the current premium provider.
     *
     * @return The premium provider
     */
    PremiumProvider getPremiumProvider();

    /**
     * Gets the plugin's logger.
     *
     * @return The logger
     */
    Logger getLogger();

    /**
     * Gets a plugin resource as an input stream.
     *
     * @param name The name of the resource
     * @return The input stream, or null if the resource does not exist
     */
    InputStream getResourceAsStream(String name);

    /**
     * Gets the plugin's configuration.
     *
     * @return The configuration
     */
    PluginConfiguration getConfiguration();

    /**
     * Gets the plugin's authorization provider.
     *
     * @return The authorization provider
     */
    AuthorizationProvider<P> getAuthorizationProvider();

    /**
     * Gets the plugin's event provider.
     *
     * @return The event provider
     */
    EventProvider<P, S> getEventProvider();

    /**
     * Gets the plugin's message provider.
     *
     * @return The message provider
     */
    Messages getMessages();

    /**
     * Gets the plugin's crypto provider by the algorithm.
     *
     * @param id The algorithm ID
     * @return The crypto provider
     */
    CryptoProvider getCryptoProvider(String id);

    /**
     * Gets the default crypto provider.
     *
     * @return The default crypto provider
     */
    CryptoProvider getDefaultCryptoProvider();

    /**
     * Gets the plugin's database provider.
     *
     * @return The database provider
     */
    ReadWriteDatabaseProvider getDatabaseProvider();

    /**
     * Gets the plugin's TOTP provider.
     *
     * @return The TOTP provider
     */
    TOTPProvider getTOTPProvider();

    /**
     * Gets the plugin's image projector.
     *
     * @return The image projector
     */
    ImageProjector<P> getImageProjector();

    /**
     * Allows you to migrate the database.
     *
     * @param from The database to migrate from
     * @param to   The database to migrate to
     */
    void migrate(ReadDatabaseProvider from, WriteDatabaseProvider to);

    /**
     * Gets the read providers
     *
     * @return The read providers
     */
    Collection<ReadDatabaseProvider> getReadProviders();

    /**
     * Allows you to use your own read providers.
     *
     * @param provider The read provider
     * @param id       The ID of the provider
     */
    void registerReadProvider(ReadDatabaseProvider provider, String id);

    /**
     * Allows you to use your own crypto algorithms.
     *
     * @param provider The crypto provider to register
     */
    void registerCryptoProvider(CryptoProvider provider);

    /**
     * Gets the data folder of the plugin.
     *
     * @return The data folder
     */
    File getDataFolder();

    /**
     * Checks whether the data folder exists.
     */
    void checkDataFolder();

    /**
     * Gets the plugin's version.
     *
     * @return The version
     */
    String getVersion();

    /**
     * Gets the plugin's parsed version.
     *
     * @return The parsed version
     */
    SemanticVersion getParsedVersion();

    /**
     * Checks whether a player with this UUID is present on the network.
     *
     * @param uuid The UUID of the player
     * @return Whether the player is present
     */
    boolean isPresent(UUID uuid);

    /**
     * Checks whether multi-proxy support is enabled.
     *
     * @return Whether multi-proxy support is enabled
     */
    boolean multiProxyEnabled();

    /**
     * Checks whether the password is fine to use.
     *
     * @param password The password to check
     * @return Whether the password is fine to use
     */
    boolean validPassword(String password);

    /**
     * Gets a player by their UUID.
     * <b>This cannot be used as a substitute to {@link #isPresent(UUID)} Due to the possibility of multiple proxies.</b>
     *
     * @param uuid The UUID of the player
     * @return The player, or null if the player is not present on this proxy
     */
    P getPlayerForUUID(UUID uuid);

    /**
     * Gets the platform handle.
     *
     * @return The platform handle
     */
    PlatformHandle<P, S> getPlatformHandle();
}
