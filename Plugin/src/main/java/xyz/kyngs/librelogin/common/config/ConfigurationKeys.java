package xyz.kyngs.librelogin.common.config;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import xyz.kyngs.librelogin.common.config.key.ConfigurationKey;

import java.util.List;

/**
 * All the keys for the configuration.
 * BTW: Most of the comments were generated by GitHub's Copilot :D
 */
public class ConfigurationKeys {

    public static final ConfigurationKey<List<String>> ALLOWED_COMMANDS_WHILE_UNAUTHORIZED = new ConfigurationKey<>(
            "allowed-commands-while-unauthorized",
            List.of(
                    "login",
                    "register",
                    "2fa",
                    "2faconfirm"
            ),
            "Commands that are allowed while the user is not authorized.",
            ConfigurateHelper::getStringList
    );

    public static final ConfigurationKey<List<String>> LIMBO = new ConfigurationKey<>(
            "limbo",
            List.of("limbo0", "limbo1"),
            "The authentication servers/worlds, players should be sent to, when not authenticated. On Paper, players will be spawned on the world spawn. THIS SERVERS MUST BE REGISTERED IN THE PROXY CONFIG. IN CASE OF PAPER, THE WORLDS MUST EXIST.",
            ConfigurateHelper::getStringList
    );

    public static final Multimap<String, String> PASS_THROUGH_DEFAULT = HashMultimap.create();
    public static final ConfigurationKey<Multimap<String, String>> PASS_THROUGH = new ConfigurationKey<>(
            "pass-through",
            PASS_THROUGH_DEFAULT,
            """
                    !!WHEN USING PAPER, PUT ALL WORLDS UNDER "root"!!
                    On Paper, players will be spawned on the world spawn.
                                        
                    The servers/worlds player should be sent to when they are authenticated. THE SERVERS MUST BE REGISTERED IN THE PROXY CONFIG. IN CASE OF PAPER, THE WORLDS MUST EXIST.
                    The configuration allows configuring forced hosts; the servers/worlds in "root" are used when players do not connect from a forced host. Use § instead of dots.
                    See: https://github.com/kyngs/LibrePremium/wiki/Configuring-Servers
                    """,
            ConfigurateHelper::getServerMap
    );

    static {
        PASS_THROUGH_DEFAULT.put("root", "lobby0");
        PASS_THROUGH_DEFAULT.put("root", "lobby1");
    }

    public static final ConfigurationKey<String> DEFAULT_CRYPTO_PROVIDER = new ConfigurationKey<>(
            "default-crypto-provider",
            "BCrypt-2A",
            """
                    The default crypto provider. This is used for hashing passwords. Available Providers:
                    SHA-256 - Older, not recommended. Kept for compatibility reasons.
                    SHA-512 - More safer than SHA-256, but still not recommended. Kept for compatibility reasons.
                    BCrypt-2A - Newer, more safe, recommended
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<Boolean> KICK_ON_WRONG_PASSWORD = new ConfigurationKey<>(
            "kick-on-wrong-password",
            false,
            "Kick the player, if the password is wrong.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<Boolean> USE_TITLES = new ConfigurationKey<>(
            "use-titles",
            true,
            "Whether or not to use titles when player is awaiting authentication.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<String> NEW_UUID_CREATOR = new ConfigurationKey<>(
            "new-uuid-creator",
            "CRACKED",
            """
                    Sets which method should be used for creating fixed UUID when a new player is created.
                    See the wiki for further information: https://github.com/kyngs/LibreLogin/wiki/UUID-Creators
                    Available Creators:
                    RANDOM - Generates a random UUID
                    CRACKED - Generates a UUID based on the player's name, the same method as if the server was in offline mode
                    MOJANG - If the player exists in the Mojang's database, it will be used. Otherwise, CRACKED will be used.
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<Boolean> AUTO_REGISTER = new ConfigurationKey<>(
            "auto-register",
            false,
            """
                    Should we automatically register all players with a premium nickname?
                    !!CRACKED PLAYERS WILL NOT BE ABLE TO REGISTER PREMIUM USERNAMES!!
                    """,
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<Integer> MILLISECONDS_TO_REFRESH_NOTIFICATION = new ConfigurationKey<>(
            "milliseconds-to-refresh-notification",
            10000,
            """
                    This specifies how often players should be notified when not authenticated. Set to negative to disable.
                    This includes (but is not limited to):
                    - Message in chat
                    - Title
                    """,
            ConfigurateHelper::getInt
    );

    public static final ConfigurationKey<Integer> SECONDS_TO_AUTHORIZE = new ConfigurationKey<>(
            "seconds-to-authorize",
            -1,
            "Sets the login/register time limit in seconds. Set to negative to disable.",
            ConfigurateHelper::getInt
    );

    public static final ConfigurationKey<?> DATABASE = ConfigurationKey.getComment(
            "database",
            "This section is used for MySQL database configuration."
    );

    public static final ConfigurationKey<String> DATABASE_TYPE = new ConfigurationKey<>(
            "database.type",
            "librelogin-sqlite",
            """
                    The type of the database. Built-in types:
                    librelogin-mysql - MySQL database, you must fill out the mysql section below.
                    librelogin-sqlite - SQLite database, default file is "database.db", you can change it in the sqlite section below.
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<?> MIGRATION = ConfigurationKey.getComment(
            "migration",
            """
                    This is used for migrating the database from other plugins.
                    Please see the wiki for further information: https://github.com/kyngs/LibreLogin/wiki/Database-Migration
                    """
    );

    public static final ConfigurationKey<Boolean> MIGRATION_ON_NEXT_STARTUP = new ConfigurationKey<>(
            "migration.on-next-startup",
            false,
            "Migrate the database on the next startup.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<String> MIGRATION_TYPE = new ConfigurationKey<>(
            "migration.type",
            "authme-sqlite",
            """
                    The type of the migration. Available Types:
                    jpremium-mysql - Can convert from MySQL JPremium SHA256 and BCrypt
                    authme-mysql - Can convert from MySQL AuthMe BCrypt and SHA256
                    authme-sqlite - Can convert from SQLite AuthMe BCrypt and SHA256
                    aegis-mysql - Can convert from MySQL Aegis BCrypt
                    dba-mysql - Can convert from MySQL DynamicBungeeAuth, which was configured to use SHA-512
                    librelogin-mysql - Can convert from MySQL LibreLogin, useful for migrating to a different database
                    librelogin-sqlite - Can convert from SQLite LibreLogin, useful for migrating to a different database
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> MIGRATION_OLD_DATABASE_TABLE = new ConfigurationKey<>(
            "migration.old-database.mysql.table",
            "user-data",
            "The table of the old database.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<?> TOTP = ConfigurationKey.getComment(
            "totp",
            """
                    This section is used for 2FA configuration.
                    !! YOU MUST HAVE PROTOCOLIZE INSTALLED FOR THIS TO WORK !!
                                        
                    You can find more information on the wiki: https://github.com/kyngs/LibreLogin/wiki/2FA
                    """
    );

    public static final ConfigurationKey<Boolean> TOTP_ENABLED = new ConfigurationKey<>(
            "totp.enabled",
            true,
            """
                    Should we enable TOTP-Based Two-Factor Authentication? If you don't know what this is, this is the 2FA used in applications like Google Authenticator etc.
                    I heavily suggest you to read this wiki page: https://github.com/kyngs/LibreLogin/wiki/2FA
                    """,
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<String> TOTP_LABEL = new ConfigurationKey<>(
            "totp.label",
            "LibreLogin Network",
            "The label to be displayed in the 2FA app. Change this to your network name.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<Integer> MINIMUM_PASSWORD_LENGTH = new ConfigurationKey<>(
            "minimum-password-length",
            -1,
            "The minimum length of a password. Set to negative to disable.",
            ConfigurateHelper::getInt
    );

    public static final ConfigurationKey<Integer> MINIMUM_USERNAME_LENGTH = new ConfigurationKey<>(
            "minimum-username-length",
            -1,
            "The minimum length the player's name can have. Only applies to new players, set to 0 or lower to disable.",
            ConfigurateHelper::getInt
    );

    public static final ConfigurationKey<Long> SESSION_TIMEOUT = new ConfigurationKey<>(
            "session-timeout",
            604800L,
            "Defines a time in seconds after a player's session expires. Default value is one week (604800 seconds). Set to zero or less to disable sessions.",
            ConfigurateHelper::getLong
    );

    public static final ConfigurationKey<Boolean> PING_SERVERS = new ConfigurationKey<>(
            "ping-servers",
            true,
            "!!THIS OPTION IS IRRELEVANT WHEN USING PAPER!! Should we ping servers to check if they are online, and get their player count? If you disable this, the pinging servers message will still appear in the console, even though the servers will not be pinged.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<Boolean> REMEMBER_LAST_SERVER = new ConfigurationKey<>(
            "remember-last-server",
            false,
            "Should we remember the last server/world a player was on? This is not recommended for large networks.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<Boolean> DEBUG = new ConfigurationKey<>(
            "debug",
            false,
            "Should we enable debug mode? This will print out debug messages to the console.",
            ConfigurateHelper::getBoolean
    );

    public static final ConfigurationKey<Boolean> FALLBACK = new ConfigurationKey<>(
            "fallback",
            true,
            "!!THIS OPTION IS IRRELEVANT WHEN USING PAPER!! Should we fallback players to lobby servers if the server they are on shutdowns? If set to false, they will be kicked.",
            ConfigurateHelper::getBoolean
    );
}