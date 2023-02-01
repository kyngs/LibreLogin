package xyz.kyngs.librepremium.common.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.MessageKeys;
import net.kyori.adventure.text.TextComponent;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.authorization.AuthenticAuthorizationProvider;
import xyz.kyngs.librepremium.common.database.MySQLDatabaseProvider;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.util.concurrent.CompletionStage;

public class Command<P> extends BaseCommand {

    protected final AuthenticLibrePremium<P, ?> plugin;

    public Command(AuthenticLibrePremium<P, ?> plugin) {
        this.plugin = plugin;
    }

    protected MySQLDatabaseProvider getDatabaseProvider() {
        return plugin.getDatabaseProvider();
    }

    protected Logger getLogger() {
        return plugin.getLogger();
    }

    protected Messages getMessages() {
        return plugin.getMessages();
    }

    protected TextComponent getMessage(String key, String... replacements) {
        return getMessages().getMessage(key, replacements);
    }

    protected AuthenticAuthorizationProvider<P, ?> getAuthorizationProvider() {
        return plugin.getAuthorizationProvider();
    }

    protected void checkAuthorized(P player) {
        if (!getAuthorizationProvider().isAuthorized(player)) {
            throw new InvalidCommandArgument(getMessage("error-not-authorized"));
        }
    }

    protected CryptoProvider getCrypto(HashedPassword password) {
        return plugin.getCryptoProvider(password.algo());
    }

    public CompletionStage<Void> runAsync(Runnable runnable) {
        return GeneralUtil.runAsync(runnable);
    }

    protected User getUser(P player) {
        if (player == null)
            throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

        var uuid = plugin.getPlatformHandle().getUUIDForPlayer(player);

        if (plugin.fromFloodgate(uuid)) throw new InvalidCommandArgument(getMessage("error-from-floodgate"));

        return plugin.getDatabaseProvider().getByUUID(uuid);
    }

}
