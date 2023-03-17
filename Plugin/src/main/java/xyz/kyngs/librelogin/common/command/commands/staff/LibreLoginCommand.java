package xyz.kyngs.librelogin.common.command.commands.staff;

import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.api.event.events.PremiumLoginSwitchEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.config.CorruptedConfigurationException;
import xyz.kyngs.librelogin.common.database.AuthenticUser;
import xyz.kyngs.librelogin.common.event.events.AuthenticPremiumLoginSwitchEvent;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import javax.annotation.Syntax;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.CompletionStage;

import static xyz.kyngs.librelogin.common.AuthenticLibreLogin.DATE_TIME_FORMATTER;

@CommandAlias("librelogin")
public class LibreLoginCommand<P> extends StaffCommand<P> {

    public LibreLoginCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Subcommand("reload configuration")
    @CommandPermission("librepremium.reload.configuration")
    public CompletionStage<Void> onReloadConfiguration(Audience audience) {
        return runAsync(() -> {
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
        });
    }

    @Subcommand("reload messages")
    @CommandPermission("librepremium.reload.messages")
    public CompletionStage<Void> onReloadMessages(Audience audience) {
        return runAsync(() -> {
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

            plugin.getCommandProvider().injectMessages();

            audience.sendMessage(getMessage("info-reloaded"));
        });
    }

    @Subcommand("user info")
    @CommandPermission("librepremium.user.info")
    @Syntax("{@@syntax.user-info}")
    @CommandCompletion("%autocomplete.user-info")
    public CompletionStage<Void> onUserInfo(Audience audience, String name) {
        return runAsync(() -> {
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
        });
    }

    public static <P> void enablePremium(P player, User user, AuthenticLibreLogin<P, ?> plugin) {
        var id = plugin.getUserOrThrowICA(user.getLastNickname());

        if (id == null) throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-not-paid"));

        user.setPremiumUUID(id.uuid());

        plugin.getEventProvider().fire(PremiumLoginSwitchEvent.class, new AuthenticPremiumLoginSwitchEvent<>(user, player, plugin));
    }

    @Subcommand("user migrate")
    @CommandPermission("librepremium.user.migrate")
    @Syntax("{@@syntax.user-migrate}")
    @CommandCompletion("%autocomplete.user-migrate")
    public CompletionStage<Void> onUserMigrate(Audience audience, String name, String newName) {
        return runAsync(() -> {
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
        });
    }

    @Subcommand("user unregister")
    @CommandPermission("librepremium.user.unregister")
    @Syntax("{@@syntax.user-unregister}")
    @CommandCompletion("%autocomplete.user-unregister")
    public CompletionStage<Void> onUserUnregister(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            requireOffline(user);

            audience.sendMessage(getMessage("info-editing"));

            user.setHashedPassword(null);
            user.setSecret(null);
            user.setIp(null);
            user.setLastAuthentication(null);
            user.setPremiumUUID(null);
            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

    @Subcommand("user delete")
    @CommandPermission("librepremium.user.delete")
    @Syntax("{@@syntax.user-delete}")
    @CommandCompletion("%autocomplete.user-delete")
    public CompletionStage<Void> onUserDelete(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            requireOffline(user);

            audience.sendMessage(getMessage("info-deleting"));

            getDatabaseProvider().deleteUser(user);

            audience.sendMessage(getMessage("info-deleted"));
        });
    }

    @Subcommand("user premium")
    @CommandPermission("librepremium.user.premium")
    @Syntax("{@@syntax.user-premium}")
    @CommandCompletion("%autocomplete.user-premium")
    public CompletionStage<Void> onUserPremium(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            requireOffline(user);

            audience.sendMessage(getMessage("info-editing"));

            enablePremium(null, user, plugin);

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

    @Subcommand("user cracked")
    @CommandPermission("librepremium.user.cracked")
    @Syntax("{@@syntax.user-cracked}")
    @CommandCompletion("%autocomplete.user-cracked")
    public CompletionStage<Void> onUserCracked(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            requireOffline(user);

            audience.sendMessage(getMessage("info-editing"));

            user.setPremiumUUID(null);
            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

    @Subcommand("user register")
    @CommandPermission("librepremium.user.register")
    @Syntax("{@@syntax.user-register}")
    @CommandCompletion("%autocomplete.user-register")
    public CompletionStage<Void> onUserRegister(Audience audience, String name, String password) {
        return runAsync(() -> {
            audience.sendMessage(getMessage("info-registering"));

            var user = getDatabaseProvider().getByName(name);

            if (user != null) {
                throw new InvalidCommandArgument(getMessage("error-occupied-user"));
            }

            user = new AuthenticUser(
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
        });
    }

    @Subcommand("user login")
    @CommandPermission("librepremium.user.login")
    @Syntax("{@@syntax.user-login}")
    @CommandCompletion("%autocomplete.user-login")
    public CompletionStage<Void> onUserLogin(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            var target = requireOnline(user);
            requireUnAuthorized(target);
            requireRegistered(user);

            audience.sendMessage(getMessage("info-logging-in"));

            plugin.getAuthorizationProvider().authorize(user, target, AuthenticatedEvent.AuthenticationReason.LOGIN);

            audience.sendMessage(getMessage("info-logged-in"));
        });
    }

    @Subcommand("user 2faoff")
    @CommandPermission("librepremium.user.2faoff")
    @Syntax("{@@syntax.user-2fa-off}")
    @CommandCompletion("%autocomplete.user-2fa-off")
    public CompletionStage<Void> onUser2FAOff(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            audience.sendMessage(getMessage("info-editing"));

            user.setSecret(null);

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

    @Subcommand("user pass-change")
    @CommandPermission("librepremium.user.pass-change")
    @Syntax("{@@syntax.user-pass-change}")
    @CommandCompletion("%autocomplete.user-pass-change")
    public CompletionStage<Void> onUserPasswordChange(Audience audience, String name, String password) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            audience.sendMessage(getMessage("info-editing"));

            var defaultProvider = plugin.getDefaultCryptoProvider();

            user.setHashedPassword(defaultProvider.createHash(password));

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

}
