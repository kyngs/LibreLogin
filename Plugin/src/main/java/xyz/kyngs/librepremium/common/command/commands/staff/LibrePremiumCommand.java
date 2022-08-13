package xyz.kyngs.librepremium.common.command.commands.staff;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librepremium.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librepremium.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.InvalidCommandArgument;
import xyz.kyngs.librepremium.common.event.events.AuthenticPremiumLoginSwitchEvent;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import javax.annotation.Syntax;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import static xyz.kyngs.librepremium.common.AuthenticLibrePremium.DATE_TIME_FORMATTER;

@CommandAlias("librepremium")
public class LibrePremiumCommand<P> extends StaffCommand<P> {

    public LibrePremiumCommand(AuthenticLibrePremium<P, ?> plugin) {
        super(plugin);
    }

    @Subcommand("reload configuration")
    @CommandPermission("librepremium.reload.configuration")
    public void onReloadConfiguration(Audience audience) {

        audience.sendMessage(getMessage("info-reloading"));

        try {
            plugin.getConfiguration().reload(plugin);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidCommandArgument(getMessage("error-unknown"));
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            throw new InvalidCommandArgument(getMessage("error-corrupted-configuration",
                    "%cause%", "%s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()))
            );
        }

        audience.sendMessage(getMessage("info-reloaded"));

    }

    @Subcommand("reload messages")
    @CommandPermission("librepremium.reload.messages")
    public void onReloadMessages(Audience audience) {

        audience.sendMessage(getMessage("info-reloading"));

        try {
            plugin.getMessages().reload(plugin);
        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidCommandArgument(getMessage("error-unknown"));
        } catch (CorruptedConfigurationException e) {
            var cause = GeneralUtil.getFurthestCause(e);
            throw new InvalidCommandArgument(getMessage("error-corrupted-messages",
                    "%cause%", "%s: %s".formatted(cause.getClass().getSimpleName(), cause.getMessage()))
            );
        }

        audience.sendMessage(getMessage("info-reloaded"));

    }

    @Subcommand("user info")
    @CommandPermission("librepremium.user.info")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserInfo(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        audience.sendMessage(getMessage("info-user",
                "%uuid%", user.getUuid().toString(),
                "%premium_uuid%", user.getPremiumUUID() == null ? "N/A" : user.getPremiumUUID().toString(),
                "%last_seen%", DATE_TIME_FORMATTER.format(user.getLastSeen().toLocalDateTime()),
                "%joined%", DATE_TIME_FORMATTER.format(user.getJoinDate().toLocalDateTime()),
                "%2fa%", user.getSecret() != null ? "Enabled" : "Disabled",
                "%ip%", user.getIp() == null ? "N/A" : user.getIp(),
                "%last_authenticated%", user.getLastAuthentication() == null ? "N/A" : DATE_TIME_FORMATTER.format(user.getLastAuthentication().toLocalDateTime())
        ));
    }

    public static <P> void enablePremium(P player, User user, AuthenticLibrePremium<P, ?> plugin) {
        var id = plugin.getUserOrThrowICA(user.getLastNickname());

        if (id == null) throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-not-paid"));

        user.setPremiumUUID(id.uuid());

        plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent<>(user, player, plugin));
    }

    @Subcommand("user migrate")
    @CommandPermission("librepremium.user.migrate")
    @Syntax("<name> <newName>")
    @CommandCompletion("@players newName")
    public void onUserMigrate(Audience audience, String name, String newName) {
        var user = getUserOtherWiseInform(name);
        var colliding = getDatabaseProvider().getByName(newName);

        if (colliding != null && !colliding.getUuid().equals(user.getUuid()))
            throw new InvalidCommandArgument(getMessage("error-occupied-user",
                    "%name%", newName
            ));

        requireOffline(user);

        audience.sendMessage(getMessage("info-editing"));

        user.setLastNickname(newName);
        if (user.getPremiumUUID() != null) {
            user.setPremiumUUID(null);
            plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent<>(user, null, plugin));
        }
        getDatabaseProvider().updateUser(user);

        audience.sendMessage(getMessage("info-edited"));
    }

    @Subcommand("user unregister")
    @CommandPermission("librepremium.user.unregister")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserUnregister(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        requireOffline(user);

        audience.sendMessage(getMessage("info-editing"));

        user.setHashedPassword(null);
        getDatabaseProvider().updateUser(user);

        audience.sendMessage(getMessage("info-edited"));
    }

    @Subcommand("user delete")
    @CommandPermission("librepremium.user.delete")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserDelete(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        requireOffline(user);

        audience.sendMessage(getMessage("info-deleting"));

        getDatabaseProvider().deleteUser(user);

        audience.sendMessage(getMessage("info-deleted"));
    }

    @Subcommand("user premium")
    @CommandPermission("librepremium.user.premium")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserPremium(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        requireOffline(user);

        audience.sendMessage(getMessage("info-editing"));

        enablePremium(null, user, plugin);

        getDatabaseProvider().updateUser(user);

        audience.sendMessage(getMessage("info-edited"));
    }

    @Subcommand("user cracked")
    @CommandPermission("librepremium.user.cracked")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserCracked(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        requireOffline(user);

        audience.sendMessage(getMessage("info-editing"));

        user.setPremiumUUID(null);
        getDatabaseProvider().updateUser(user);

        audience.sendMessage(getMessage("info-edited"));
    }

    @Subcommand("user register")
    @CommandPermission("librepremium.user.register")
    @Syntax("<name> <password>")
    @CommandCompletion("@players password")
    public void onUserRegister(Audience audience, String name, String password) {
        audience.sendMessage(getMessage("info-registering"));

        var user = getDatabaseProvider().getByName(name);

        if (user != null) {
            throw new InvalidCommandArgument(getMessage("error-occupied-user"));
        }

        user = new User(
                plugin.generateNewUUID(name, plugin.getUserOrThrowICA(name).uuid()),
                null,
                plugin.getDefaultCryptoProvider().createHash(password),
                name,
                Timestamp.valueOf(LocalDateTime.now()),
                Timestamp.valueOf(LocalDateTime.now()),
                null,
                null,
                Timestamp.valueOf(LocalDateTime.now()),
                null
        );

        getDatabaseProvider().insertUser(user);

        audience.sendMessage(getMessage("info-registered"));
    }

    @Subcommand("user login")
    @CommandPermission("librepremium.user.login")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUserLogin(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        var target = requireOnline(user);
        requireUnAuthorized(target);
        requireRegistered(user);

        audience.sendMessage(getMessage("info-logging-in"));

        plugin.getAuthorizationProvider().authorize(user, target, AuthenticatedEvent.AuthenticationReason.LOGIN);

        audience.sendMessage(getMessage("info-logged-in"));
    }

    @Subcommand("user 2faoff")
    @CommandPermission("librepremium.user.2faoff")
    @Syntax("<name>")
    @CommandCompletion("@players")
    public void onUser2FAOff(Audience audience, String name) {
        var user = getUserOtherWiseInform(name);

        audience.sendMessage(getMessage("info-editing"));

        user.setSecret(null);

        getDatabaseProvider().updateUser(user);

        audience.sendMessage(getMessage("info-edited"));
    }

}
