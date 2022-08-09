package xyz.kyngs.librepremium.common.command;

import co.aikar.commands.CommandManager;
import co.aikar.commands.MessageKeys;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import xyz.kyngs.librepremium.api.database.User;
import xyz.kyngs.librepremium.common.AuthenticHandler;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.command.commands.ChangePasswordCommand;
import xyz.kyngs.librepremium.common.command.commands.authorization.LoginCommand;
import xyz.kyngs.librepremium.common.command.commands.authorization.RegisterCommand;
import xyz.kyngs.librepremium.common.command.commands.premium.PremiumConfirmCommand;
import xyz.kyngs.librepremium.common.command.commands.premium.PremiumDisableCommand;
import xyz.kyngs.librepremium.common.command.commands.premium.PremiumEnableCommand;
import xyz.kyngs.librepremium.common.command.commands.staff.LibrePremiumCommand;
import xyz.kyngs.librepremium.common.command.commands.tfa.TwoFactorAuthCommand;
import xyz.kyngs.librepremium.common.command.commands.tfa.TwoFactorConfirmCommand;
import xyz.kyngs.librepremium.common.util.RateLimiter;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommandProvider<P, S> extends AuthenticHandler<P, S> {

    public static final LegacyComponentSerializer ACF_SERIALIZER = LegacyComponentSerializer.legacySection();

    private final CommandManager<?, ?, ?, ?, ?, ?> manager;
    private final RateLimiter<UUID> limiter;
    private final Cache<UUID, Object> confirmCache;

    public CommandProvider(AuthenticLibrePremium<P, S> plugin) {
        super(plugin);

        limiter = new RateLimiter<>(1, TimeUnit.SECONDS);

        manager = plugin.provideManager();

        var locales = manager.getLocales();

        var localeMap = new HashMap<String, String>();

        localeMap.put("acf-core.permission_denied", getMessageAsString("error-no-permission"));
        localeMap.put("acf-core.permission_denied_parameter", getMessageAsString("error-no-permission"));
        localeMap.put("acf-core.invalid_syntax", getMessageAsString("error-invalid-syntax"));
        localeMap.put("acf-core.unknown_command", getMessageAsString("error-unknown-command"));

        locales.addMessageStrings(locales.getDefaultLocale(), localeMap);

        var contexts = manager.getCommandContexts();

        contexts.registerIssuerAwareContext(User.class, context -> {
            var player = plugin.getPlayerFromIssuer(context.getIssuer());

            if (player == null)
                throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

            var uuid = plugin.getPlatformHandle().getUUIDForPlayer(player);

            if (plugin.fromFloodgate(uuid)) throw new InvalidCommandArgument(getMessage("error-from-floodgate"));

            return plugin.getDatabaseProvider().getByUUID(uuid);
        });

        contexts.registerIssuerAwareContext(Audience.class, context -> {
            if (limiter.tryAndLimit(context.getIssuer().getUniqueId()))
                throw new xyz.kyngs.librepremium.common.command.InvalidCommandArgument(plugin.getMessages().getMessage("error-throttle"));
            return plugin.getAudienceFromIssuer(context.getIssuer());
        });

        // Thanks type erasure
        contexts.registerIssuerAwareContext(Object.class, context -> {
            var player = plugin.getPlayerFromIssuer(context.getIssuer());

            if (player == null)
                throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

            return player;
        });

        contexts.registerIssuerAwareContext(UUID.class, context -> {
            var player = plugin.getPlayerFromIssuer(context.getIssuer());

            if (player == null)
                throw new co.aikar.commands.InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE, false);

            return plugin.getPlatformHandle().getUUIDForPlayer(player);
        });

        manager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            if (!(t instanceof xyz.kyngs.librepremium.common.command.InvalidCommandArgument ourEx)) {
                var logger = plugin.getLogger();

                logger.error("An unexpected exception occurred while performing command, please attach the stacktrace below and report this issue.");

                t.printStackTrace();

                return false;
            }

            plugin.getAudienceFromIssuer(sender).sendMessage(ourEx.getUserFuckUp());

            return true;
        }, false);

        confirmCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();

        manager.registerCommand(new LoginCommand<>(plugin));
        manager.registerCommand(new RegisterCommand<>(plugin));
        manager.registerCommand(new PremiumEnableCommand<>(plugin));
        manager.registerCommand(new PremiumConfirmCommand<>(plugin));
        manager.registerCommand(new PremiumDisableCommand<>(plugin));
        manager.registerCommand(new ChangePasswordCommand<>(plugin));
        manager.registerCommand(new LibrePremiumCommand<>(plugin));

        if (plugin.getTOTPProvider() != null) {
            manager.registerCommand(new TwoFactorAuthCommand<>(plugin));
            manager.registerCommand(new TwoFactorConfirmCommand<>(plugin));
        }

    }

    public void registerConfirm(UUID uuid) {
        confirmCache.put(uuid, new Object());
    }

    public void onConfirm(P player, Audience audience, User user) {
        if (confirmCache.asMap().remove(user.getUuid()) == null)
            throw new InvalidCommandArgument(plugin.getMessages().getMessage("error-no-confirm"));

        audience.sendMessage(plugin.getMessages().getMessage("info-enabling"));

        LibrePremiumCommand.enablePremium(player, user, plugin);

        plugin.getDatabaseProvider().updateUser(user);

        platformHandle.kick(player, plugin.getMessages().getMessage("kick-premium-info-enabled"));

    }

    public TextComponent getMessage(String key) {
        return plugin.getMessages().getMessage(key);
    }

    private String getMessageAsString(String key) {
        return ACF_SERIALIZER.serialize(getMessage(key));
    }

    public RateLimiter<UUID> getLimiter() {
        return limiter;
    }
}
