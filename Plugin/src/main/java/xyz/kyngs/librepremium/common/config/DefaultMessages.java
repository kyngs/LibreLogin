package xyz.kyngs.librepremium.common.config;

import xyz.kyngs.librepremium.common.config.key.ConfigurationKey;

/**
 * All the keys for the messages
 * BTW: Most of the comments were generated by GitHub's Copilot :D
 */
@SuppressWarnings("unused")
public class DefaultMessages {

    /*
    Kicks related to autologin
     */

    public static final ConfigurationKey<String> KICK_PREMIUM_ERROR_THROTTLED = new ConfigurationKey<>(
            "kick-premium-error-throttled",
            "The Mojang API is rate limiting our server, please try joining again in a while!",
            "This message is displayed when the Mojang API is rate limiting us, therefore we cannot verify whether there is an account with the given username.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_PREMIUM_ERROR_UNDEFINED = new ConfigurationKey<>(
            "kick-premium-error-undefined",
            "There was some issue while communicating with Mojang, if the problem persists, contact the server administrators!",
            """
                    This message is displayed when there was an unknown issue while communicating with Mojang, therefore we cannot verify whether there is an account with the given username.
                    The error will be printed to the console
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_PREMIUM_INFO_ENABLED = new ConfigurationKey<>(
            "kick-premium-info-enabled",
            "Autologin enabled!",
            "This message is displayed when the player enables autologin, they need to be kicked.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_PREMIUM_INFO_DISABLED = new ConfigurationKey<>(
            "kick-premium-info-disabled",
            "Autologin disabled!",
            "This message is displayed when the player disables autologin, they need to be kicked.",
            ConfigurateHelper::getString
    );

    /*
    Kicks related to username
     */

    public static final ConfigurationKey<String> KICK_INVALID_CASE_USERNAME = new ConfigurationKey<>(
            "kick-invalid-case-username",
            "Please, change your username to &c%username%",
            """
                    This message is displayed when the player's username is not in the correct case.
                    See https://github.com/kyngs/LibrePremium/wiki/Name-Validation for more information.
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_OCCUPIED_USERNAME = new ConfigurationKey<>(
            "kick-occupied-username",
            "Please, change your username to &c%username%",
            """
                    This message is displayed when the player's username is already taken.
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_ILLEGAL_USERNAME = new ConfigurationKey<>(
            "kick-illegal-username",
            "You have illegal characters in your username or/and your username is longer than 16 characters!",
            """
                    This message is displayed when the player's username is not allowed.
                    See https://github.com/kyngs/LibrePremium/wiki/Name-Validation for more information.
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_NAME_MISMATCH = new ConfigurationKey<>(
            "kick-name-mismatch",
            "Oh no! It looks like an premium user with activated auto login changed their nickname to %nickname%, therefore there are 2 colliding accounts. Please contact support immediately.",
            """
                    This occurs, when there is a profile conflict.
                    See https://github.com/kyngs/LibrePremium/wiki/Profile-Conflicts for more information.
                    """,
            ConfigurateHelper::getString
    );

    /*
    Other kicks
     */

    public static final ConfigurationKey<String> KICK_NO_SERVER = new ConfigurationKey<>(
            "kick-no-server",
            "There's no available server to connect you to. Please try again later.",
            "This message is displayed when the player is kicked because there is not any server available.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_TIME_LIMIT = new ConfigurationKey<>(
            "kick-time-limit",
            "You took too long to authorize!",
            "This message is displayed when the player takes too long to authorize. (You can specify this time limit in the config file)",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> KICK_ERROR_PASSWORD_WRONG = new ConfigurationKey<>(
            "kick-error-password-wrong",
            "Wrong password!",
            "This message is displayed when the player is kicked because they authorize with wrong password.",
            ConfigurateHelper::getString
    );

    /*
    Errors related to commands
     */

    public static final ConfigurationKey<String> ERROR_FROM_FLOODGATE = new ConfigurationKey<>(
            "error-from-floodgate",
            "You can't use this command from Floodgate!",
            "This message is displayed when the player is executing a command, but they are coming from Floodgate.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_ALREADY_AUTHORIZED = new ConfigurationKey<>(
            "error-already-authorized",
            "You are already authorized!",
            "This message is displayed when the player tries to authorize again.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_ALREADY_REGISTERED = new ConfigurationKey<>(
            "error-already-registered",
            "You are already registered!",
            "This message is displayed when the player tries to register again.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_AUTHORIZED = new ConfigurationKey<>(
            "error-not-authorized",
            "Please authorize first!",
            "This message is displayed when the player tries to use a command that requires authorization.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_REGISTERED = new ConfigurationKey<>(
            "error-not-registered",
            "Please register first!",
            "This message is displayed when the player tries to login, when they are not registered.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_PAID = new ConfigurationKey<>(
            "error-not-paid",
            "This account does not exist in the Mojang database!",
            "This message is displayed when the player tries to enable autologin, but their account does not exist in the Mojang database.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_PREMIUM = new ConfigurationKey<>(
            "error-not-premium",
            "You do not have autologin enabled, enable it using /premium <password>!",
            "This message is displayed when the player tries to disable autologin, but they do not have it enabled.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_CRACKED = new ConfigurationKey<>(
            "error-not-cracked",
            "You have autologin enabled, disable it using /cracked!",
            "This message is displayed when the player tries to enable autologin, but they already have it enabled.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PASSWORD_WRONG = new ConfigurationKey<>(
            "error-password-wrong",
            "Wrong password!",
            "This message is displayed when the player tries to authorize with wrong password.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> TOTP_NOT_PROVIDED = new ConfigurationKey<>(
            "totp-not-provided",
            "You must provide a 2FA code! Use /login <password> <2FA code>, if you lost your code, contact the admins.",
            "This message is displayed when the player tries to authorize without providing a 2FA code.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> TOTP_WRONG = new ConfigurationKey<>(
            "totp-wrong",
            "Wrong 2FA code!",
            "This message is displayed when the player tries to authorize with wrong 2FA code.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PASSWORD_NOT_MATCH = new ConfigurationKey<>(
            "error-password-not-match",
            "Passwords do not match!",
            "This message is displayed when the player tries to register with different passwords.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_FORBIDDEN_PASSWORD = new ConfigurationKey<>(
            "error-forbidden-password",
            "The password is too short and/or is not allowed!",
            "This message is displayed when the player tries to register with a password that is too short or forbidden.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PASSWORD_CORRUPTED = new ConfigurationKey<>(
            "error-password-corrupted",
            "Your password is corrupted, please contact an administrator!",
            "This message is displayed when the player tries to login with corrupted password.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PREMIUM_THROTTLED = new ConfigurationKey<>(
            "error-premium-throttled",
            "The Mojang API is rate limiting our server, please try the command again in a while!",
            "This message is displayed when the Mojang API is rate limiting us, therefore we cannot verify whether there is an account with the given username.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PREMIUM_UNKNOWN = new ConfigurationKey<>(
            "error-premium-unknown",
            "There was an unknown error while communicating with the mojang API, please check console for further details!",
            """
                    This message is displayed when there was an unknown issue while communicating with Mojang, therefore we cannot verify whether there is an account with the given username.
                    The error will be printed to the console
                    """,
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_THROTTLE = new ConfigurationKey<>(
            "error-throttle",
            "You are sending commands too fast! Please wait a bit.",
            "This message is displayed when the player tries to send commands too fast.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NO_CONFIRM = new ConfigurationKey<>(
            "error-no-confirm",
            "Please use /premium <password> first!",
            "This message is displayed when the player tries to run /premiumconfirm, before running /premium.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_CORRUPTED_MESSAGES = new ConfigurationKey<>(
            "error-corrupted-messages",
            "Messages are corrupted, old ones are going to be kept. Cause: %cause%",
            "This message is displayed when the messages.conf file is corrupted.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_CORRUPTED_CONFIGURATION = new ConfigurationKey<>(
            "error-corrupted-configuration",
            "Configuration is corrupted, old one is going to be kept. Cause: %cause%",
            "This message is displayed when the configuration file is corrupted.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_UNKNOWN = new ConfigurationKey<>(
            "error-unknown",
            "An unknown error occurred! Check console for further information.",
            "This message is displayed when an unknown error occurs.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_UNKNOWN_USER = new ConfigurationKey<>(
            "error-unknown-user",
            "This user does not exist!",
            "This message is displayed when someone attempts to refer an unknown user.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_OCCUPIED_USER = new ConfigurationKey<>(
            "error-occupied-user",
            "This username is already occupied!",
            "This message is displayed when someone tries to move account to an occupied username.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PLAYER_ONLINE = new ConfigurationKey<>(
            "error-player-online",
            "This player is online!",
            "This message is displayed when someone attempts to refer an online player.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PLAYER_OFFLINE = new ConfigurationKey<>(
            "error-player-offline",
            "This player is offline!",
            "This message is displayed when someone attempts to refer an offline player.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PLAYER_AUTHORIZED = new ConfigurationKey<>(
            "error-player-authorized",
            "This player is already authenticated!",
            "This message is displayed when someone attempts to refer an authenticated player.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_PLAYER_NOT_REGISTERED = new ConfigurationKey<>(
            "error-player-not-registered",
            "This player is not registered!",
            "This message is displayed when someone attempts to refer an unregistered player.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_NOT_AVAILABLE_ON_MULTI_PROXY = new ConfigurationKey<>(
            "error-not-available-on-multi-proxy",
            "This feature is not available on MultiProxy!",
            "This message is displayed when someone attempts to use a feature that is not available on MultiProxy.",
            ConfigurateHelper::getString
    );

    /*
    ACF Related errors
     */

    public static final ConfigurationKey<String> ERROR_NO_PERMISSION = new ConfigurationKey<>(
            "error-no-permission",
            "You do not have permission to use this command!",
            "This message is displayed when the player does not have permission to use a command.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_INVALID_SYNTAX = new ConfigurationKey<>(
            "error-invalid-syntax",
            "Usage: <c2>{command}</c2> <c3>{syntax}</c3>",
            "This message is displayed when the player uses a command with invalid syntax.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> ERROR_UNKNOWN_COMMAND = new ConfigurationKey<>(
            "error-unknown-command",
            "Unknown command!",
            "This message is displayed when the player uses an unknown command.",
            ConfigurateHelper::getString
    );

    /*
    General information messages
     */

    public static final ConfigurationKey<String> INFO_LOGGING_IN = new ConfigurationKey<>(
            "info-logging-in",
            "Logging in...",
            "This message is displayed when the player attempts to log in.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_LOGGED_IN = new ConfigurationKey<>(
            "info-logged-in",
            "Logged in!",
            "This message is displayed when the player logs in.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_PREMIUM_LOGGED_IN = new ConfigurationKey<>(
            "info-premium-logged-in",
            "You have been logged in automatically!",
            "This message is displayed when the player logs in automatically because they have enabled /premium.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_SESSION_LOGGED_IN = new ConfigurationKey<>(
            "info-session-logged-in",
            "You have been logged in automatically!",
            "This message is displayed when the player logs in automatically because of a valid session.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_REGISTERING = new ConfigurationKey<>(
            "info-registering",
            "Registering...",
            "This message is displayed when the player attempts to register.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_REGISTERED = new ConfigurationKey<>(
            "info-registered",
            "Registered!",
            "This message is displayed when the player registers.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_ENABLING = new ConfigurationKey<>(
            "info-enabling",
            "Enabling...",
            "This message is displayed when something is enabling.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_DISABLING = new ConfigurationKey<>(
            "info-disabling",
            "Disabling...",
            "This message is displayed when something is disabling.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_EDITING = new ConfigurationKey<>(
            "info-editing",
            "Editing...",
            "This message is displayed when something is being edited.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_EDITED = new ConfigurationKey<>(
            "info-edited",
            "Edited!",
            "This message is displayed when something has been edited.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_DELETING = new ConfigurationKey<>(
            "info-deleting",
            "Deleting...",
            "This message is displayed when something is being deleted.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_DELETED = new ConfigurationKey<>(
            "info-deleted",
            "Deleted!",
            "This message is displayed when something has been deleted.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_RELOADING = new ConfigurationKey<>(
            "info-reloading",
            "Reloading...",
            "This message is displayed when something is being reloaded.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_RELOADED = new ConfigurationKey<>(
            "info-reloaded",
            "Reloaded!",
            "This message is displayed when something has been reloaded.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> INFO_USER = new ConfigurationKey<>(
            "info-user",
            """
                    UUID: %uuid%
                    Premium UUID: %premium_uuid%
                    Last Seen: %last_seen%
                    Joined: %joined%
                    2FA: %2fa%
                    IP: %ip%
                    Last Authenticated: %last_authenticated%
                    """,
            "This message is displayed when the player's information is requested.",
            ConfigurateHelper::getString
    );

    /*
    Prompts
     */

    public static final ConfigurationKey<String> PROMPT_REGISTER = new ConfigurationKey<>(
            "prompt-register",
            "Please register using: &e/register &b<password> <password>",
            "This message is displayed when the player is prompted to register.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> PROMPT_LOGIN = new ConfigurationKey<>(
            "prompt-login",
            "Please login using: &e/login &b<password> [2fa_code]",
            "This message is displayed when the player is prompted to login.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> PROMPT_CONFIRM = new ConfigurationKey<>(
            "prompt-confirm",
            "You are about to enable premium autologin, please take note, that you &4WILL NOT&r be able to connect to your account from cracked client. You can turn this off later by using /cracked. To confirm this action, you have 5 minutes to run /confirmpremium",
            "This message is displayed when the player is prompted to confirm autologin.",
            ConfigurateHelper::getString
    );

    /*
    Titles
     */

    public static final ConfigurationKey<String> TITLE_REGISTER = new ConfigurationKey<>(
            "title-register",
            "&6&lRegister",
            "This title is displayed when the player is prompted to register. Make sure that you have use-titles set to true in the configuration.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> TITLE_LOGIN = new ConfigurationKey<>(
            "title-login",
            "&6&lLogin",
            "This title is displayed when the player is prompted to login. Make sure that you have use-titles set to true in the configuration.",
            ConfigurateHelper::getString
    );

    /*
    Subtitles
     */

    public static final ConfigurationKey<String> SUB_TITLE_REGISTER = new ConfigurationKey<>(
            "sub-title-register",
            "&e/register &b<password> <password>",
            "This subtitle is displayed when the player is prompted to register. Make sure that you have use-titles set to true in the configuration.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> SUB_TITLE_LOGIN = new ConfigurationKey<>(
            "sub-title-login",
            "&e/login &b<password>",
            "This subtitle is displayed when the player is prompted to login. Make sure that you have use-titles set to true in the configuration.",
            ConfigurateHelper::getString
    );

    /*
    TOTP
     */

    public static final ConfigurationKey<String> TOTP_SHOW_INFO = new ConfigurationKey<>(
            "totp-show-info",
            "Please scan the QR code on the map into your 2FA app. For example, Google Authenticator. When you are complete, please disconnect.",
            "This message is displayed when the player is prompted to scan the 2FA QR code.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> TOTP_GENERATING = new ConfigurationKey<>(
            "totp-generating",
            "Generating 2FA code...",
            "This message is displayed when player has begun the 2FA enable procedure.",
            ConfigurateHelper::getString
    );

    public static final ConfigurationKey<String> TOTP_WRONG_VERSION = new ConfigurationKey<>(
            "totp-wrong-version",
            "You must connect with client version %low% - %high%, in order to enable 2FA. You can then connect back with old version again.",
            "This message is displayed when the player attempts to enable 2FA with an old client.",
            ConfigurateHelper::getString
    );


}
