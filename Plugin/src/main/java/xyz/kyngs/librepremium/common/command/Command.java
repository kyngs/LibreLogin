package xyz.kyngs.librepremium.common.command;

import co.aikar.commands.BaseCommand;
import net.kyori.adventure.text.TextComponent;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.configuration.Messages;
import xyz.kyngs.librepremium.api.crypto.CryptoProvider;
import xyz.kyngs.librepremium.api.crypto.HashedPassword;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.authorization.AuthenticAuthorizationProvider;
import xyz.kyngs.librepremium.common.database.MySQLDatabaseProvider;

public class Command extends BaseCommand {

    protected final AuthenticLibrePremium plugin;

    public Command(AuthenticLibrePremium plugin) {
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

    protected AuthenticAuthorizationProvider getAuthorizationProvider() {
        return plugin.getAuthorizationProvider();
    }

    protected void checkAuthorized(User user) {
        if (!getAuthorizationProvider().isAuthorized(user.getUuid())) {
            throw new InvalidCommandArgument(getMessage("error-not-authorized"));
        }
    }

    protected CryptoProvider getCrypto(HashedPassword password) {
        return plugin.getCryptoProvider(password.algo());
    }

}
