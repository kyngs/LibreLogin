package xyz.kyngs.librepremium.api;

import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.authorization.AuthorizationProvider;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.api.configuration.PluginConfiguration;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.database.ReadDatabaseProvider;
import xyz.kyngs.librepremium.api.database.WriteDatabaseProvider;
import xyz.kyngs.librepremium.api.event.EventProvider;
import xyz.kyngs.librepremium.api.premium.PremiumProvider;
import xyz.kyngs.librepremium.api.util.SemanticVersion;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;

public interface LibrePremiumPlugin {

    PremiumProvider getPremiumProvider();

    Logger getLogger();

    InputStream getResourceAsStream(String name);

    PluginConfiguration getConfiguration();

    AuthorizationProvider getAuthorizationProvider();

    EventProvider getEventProvider();

    Messages getMessages();

    CryptoProvider getCryptoProvider(String id);

    CryptoProvider getDefaultCryptoProvider();

    ReadDatabaseProvider getDatabaseProvider();

    Audience getAudienceForID(UUID uuid);

    void migrate(ReadDatabaseProvider from, WriteDatabaseProvider to);

    Collection<ReadDatabaseProvider> getReadProviders();

    void registerReadProvider(ReadDatabaseProvider provider, String id);

    void registerCryptoProvider(CryptoProvider provider);

    File getDataFolder();

    void checkDataFolder();

    String getVersion();

    SemanticVersion getParsedVersion();

    boolean isPresent(UUID uuid);

    boolean multiProxyEnabled();

}
