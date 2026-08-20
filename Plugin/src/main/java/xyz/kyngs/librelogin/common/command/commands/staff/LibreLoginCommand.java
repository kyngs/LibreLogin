/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.staff;

import co.aikar.commands.annotation.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.audience.Audience;
import xyz.kyngs.librelogin.api.configuration.CorruptedConfigurationException;
import xyz.kyngs.librelogin.api.database.User;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;
import xyz.kyngs.librelogin.common.database.AuthenticUser;
import xyz.kyngs.librelogin.common.event.events.AuthenticPasswordChangeEvent;
import xyz.kyngs.librelogin.common.event.events.AuthenticPremiumLoginSwitchEvent;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletionStage;

import static xyz.kyngs.librelogin.common.AuthenticLibreLogin.DATE_TIME_FORMATTER;
import static xyz.kyngs.librelogin.common.AuthenticLibreLogin.GSON;

@CommandAlias("librelogin")
public class LibreLoginCommand<P> extends StaffCommand<P> {

    public LibreLoginCommand(AuthenticLibreLogin<P, ?> plugin) {
        super(plugin);
    }

    @Subcommand("about")
    @Default
    public CompletionStage<Void> onAbout(Audience audience) {
        return runAsync(() -> audience.sendMessage(getMessage("info-about",
                "%version%", plugin.getVersion()
        )));
    }

    @Subcommand("email test")
    @CommandPermission("librelogin.email.test")
    @Syntax("{@@syntax.email-test}")
    @CommandCompletion("%autocomplete.email-test")
    public CompletionStage<Void> onEmailTest(Audience audience, String email) {
        return runAsync(() -> {
            if (plugin.getEmailHandler() == null)
                throw new InvalidCommandArgument(getMessage("error-password-resetting-disabled"));
            audience.sendMessage(getMessage("info-sending-email"));
            plugin.getEmailHandler().sendTestMail(email);
            audience.sendMessage(getMessage("info-sent-email"));
        });
    }

    @Subcommand("dump")
    @CommandPermission("librelogin.dump")
    public CompletionStage<Void> onDump(Audience audience) {
        return runAsync(() -> {
            audience.sendMessage(getMessage("info-dumping"));

            var dumpFolder = new File(plugin.getDataFolder(), "dumps");

            if (!dumpFolder.exists()) {
                dumpFolder.mkdirs();
            }

            var dumpFile = new File(dumpFolder, "dump-%date%.json".replace("%date%", DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss").format(LocalDateTime.now())));

            if (dumpFile.exists()) {
                dumpFile.delete();
            }

            try {
                dumpFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                throw new InvalidCommandArgument(getMessage("error-unknown"));
            }

            var dump = new JsonObject();

            dump.addProperty("version", plugin.getVersion());
            dump.addProperty("date", DATE_TIME_FORMATTER.format(LocalDateTime.now()));

            var server = new JsonObject();

            var proxyData = plugin.getPlatformHandle().getProxyData();

            server.addProperty("name", proxyData.name());
            server.add("plugins", GSON.toJsonTree(proxyData.plugins()));
            server.add("servers", GSON.toJsonTree(proxyData.servers()));
            server.add("limbos", GSON.toJsonTree(proxyData.limbos()));
            server.add("lobbies", GSON.toJsonTree(proxyData.lobbies()));

            var threads = new JsonObject();

            var mxBean = ManagementFactory.getThreadMXBean();

            for (ThreadInfo info : mxBean.dumpAllThreads(true, true)) {
                var thread = new JsonObject();

                thread.addProperty("id", info.getThreadId());
                thread.addProperty("name", info.getThreadName());
                thread.addProperty("state", info.getThreadState().name());
                thread.addProperty("priority", info.getPriority());
                thread.addProperty("isDaemon", info.isDaemon());
                thread.addProperty("isInNative", info.isInNative());
                thread.addProperty("isSuspended", info.isSuspended());

                var lock = new JsonObject();

                if (info.getLockName() != null) {
                    lock.addProperty("name", info.getLockName());
                    lock.addProperty("ownerId", info.getLockOwnerId());
                    lock.addProperty("ownerName", info.getLockOwnerName());
                }

                thread.add("lock", lock);

                var stackTrace = new JsonArray();

                for (StackTraceElement element : info.getStackTrace()) {
                    stackTrace.add(element.getClassName() + "#" + element.getMethodName() + "#" + element.getLineNumber());
                }

                thread.add("stackTrace", stackTrace);

                threads.add(info.getThreadName(), thread);
            }

            server.add("threads", threads);

            dump.add("server", server);

            try (var writer = new FileWriter(dumpFile)) {
                writer.write(GSON.toJson(dump));
            } catch (IOException e) {
                e.printStackTrace();
                throw new InvalidCommandArgument(getMessage("error-unknown"));
            }

            audience.sendMessage(getMessage("info-dumped", "%file%", dumpFile.getPath()));
        });
    }

    @Subcommand("reload configuration")
    @CommandPermission("librepremium.reload.configuration")
    public CompletionStage<Void> onReloadConfiguration(Audience audience) {
        return runAsync(() -> {
            audience.sendMessage(getMessage("info-reloading"));

            try {
                plugin.reloadConfiguration();
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
                    "%email%", user.getEmail() == null ? "N/A" : user.getEmail(),
                    "%ip%", user.getIp() == null ? "N/A" : user.getIp(),
                    "%last_authenticated%", user.getLastAuthentication() == null ? "N/A" : DATE_TIME_FORMATTER.format(user.getLastAuthentication().toLocalDateTime())
            ));
        });
    }

    public static <P> void enablePremium(P player, User user, AuthenticLibreLogin<P, ?> plugin, boolean onlyValid) {
        var id = plugin.getUserOrThrowICA(user.getLastNickname());

        if (onlyValid && id != null && !id.reliable()) {
            plugin.getLogger().warn("Data retrieved from premium provider is not reliable for user %s, can not safely enable premium login. \nPlease verify the correct capitalization using site such as NameMC and then enable it manually using the /librelogin user premium command.".formatted(user.getLastNickname()));
            throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-not-paid"));
        }

        // Users are stupid, and sometimes they connect with a differently cased name than the one they registered with at Mojang
        if (id == null || !id.name().equals(user.getLastNickname())) {
            throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-not-paid"));
        }

        user.setPremiumUUID(id.uuid());

        plugin.getEventProvider().unsafeFire(plugin.getEventTypes().premiumLoginSwitch, new AuthenticPremiumLoginSwitchEvent<>(user, player, plugin));
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
                plugin.getEventProvider().unsafeFire(plugin.getEventTypes().premiumLoginSwitch, new AuthenticPremiumLoginSwitchEvent<>(user, null, plugin));
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

            var player = getPossiblyOnlinePlayerOnThisProxy(user);

            audience.sendMessage(getMessage("info-editing"));

            enablePremium(null, user, plugin, false);

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));

            if (player != null) plugin.getPlatformHandle().kick(player, getMessage("kick-premium-info-enabled"));
        });
    }

    @Subcommand("user cracked")
    @CommandPermission("librepremium.user.cracked")
    @Syntax("{@@syntax.user-cracked}")
    @CommandCompletion("%autocomplete.user-cracked")
    public CompletionStage<Void> onUserCracked(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            var player = getPossiblyOnlinePlayerOnThisProxy(user);

            audience.sendMessage(getMessage("info-editing"));

            user.setPremiumUUID(null);
            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));

            if (player != null) plugin.getPlatformHandle().kick(player, getMessage("kick-premium-info-disabled"));
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

            var hashedPassword = plugin.getDefaultCryptoProvider().createHash(password);

            if (hashedPassword == null) {
                throw new InvalidCommandArgument(getMessage("error-password-too-long"));
            }
            var premiumUser = plugin.getUserOrThrowICA(name);
            user = new AuthenticUser(
                    plugin.generateNewUUID(name, premiumUser == null ? null : premiumUser.uuid()),
                    null,
                    hashedPassword,
                    name,
                    Timestamp.valueOf(LocalDateTime.now()),
                    Timestamp.valueOf(LocalDateTime.now()),
                    null,
                    null,
                    Timestamp.valueOf(LocalDateTime.now()),
                    null,
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

    @Subcommand("user emailoff")
    @CommandPermission("librepremium.user.emailoff")
    @Syntax("{@@syntax.user-email-off}")
    @CommandCompletion("%autocomplete.user-email-off")
    public CompletionStage<Void> onUserEMailOff(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            audience.sendMessage(getMessage("info-editing"));

            user.setEmail(null);

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));
        });
    }

    @Subcommand("user setemail")
    @CommandPermission("librepremium.user.setemail")
    @Syntax("{@@syntax.user-set-email}")
    @CommandCompletion("%autocomplete.user-set-email")
    public CompletionStage<Void> onUserSetEMail(Audience audience, String name, String email) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            audience.sendMessage(getMessage("info-editing"));

            user.setEmail(email);

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
            var old = user.getHashedPassword();

            setPassword(audience, user, password, "info-editing");

            getDatabaseProvider().updateUser(user);

            audience.sendMessage(getMessage("info-edited"));

            plugin.getEventProvider().unsafeFire(plugin.getEventTypes().passwordChange, new AuthenticPasswordChangeEvent<>(user, null, plugin, old));
        });
    }

    @Subcommand("user alts")
    @CommandPermission("librelogin.user.alts")
    @Syntax("{@@syntax.user-alts}")
    @CommandCompletion("%autocomplete.user-alts")
    public CompletionStage<Void> onUserAlts(Audience audience, String name) {
        return runAsync(() -> {
            var user = getUserOtherWiseInform(name);

            var alts = getDatabaseProvider().getByIP(user.getIp());

            if (alts.isEmpty()) {
                audience.sendMessage(getMessage("info-no-alts"));
                return;
            }

            audience.sendMessage(getMessage("info-alts",
                    "%count%", String.valueOf(alts.size())
            ));

            for (var alt : alts) {
                audience.sendMessage(getMessage("info-alts-entry",
                        "%name%", alt.getLastNickname(),
                        "%last_seen%", DATE_TIME_FORMATTER.format(user.getLastSeen().toLocalDateTime())
                ));
            }
        });
    }

}
