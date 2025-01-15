/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.premium;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import xyz.kyngs.librelogin.api.premium.PremiumException;
import xyz.kyngs.librelogin.api.premium.PremiumProvider;
import xyz.kyngs.librelogin.api.premium.PremiumUser;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.util.GeneralUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthenticPremiumProvider implements PremiumProvider {

    private final Cache<String, PremiumUser> userCache;
    private final List<ThrowableFunction<String, PremiumUser, PremiumException>> fetchers;
    private final AuthenticLibreLogin<?, ?> plugin;

    public AuthenticPremiumProvider(AuthenticLibreLogin<?, ?> plugin) {
        this.plugin = plugin;
        userCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();

        fetchers = new ArrayList<>(3);

        fetchers.add(this::getUserFromMojang);
        fetchers.add(this::getUserFromPlayerDB);
        //fetchers.add(this::getUserFromAshcon); //Momentarily disabled, as it's unreliable. See https://github.com/Electroid/mojang-api/issues/79
    }

    @Override
    public PremiumUser getUserForName(String name) throws PremiumException {
        name = name.toLowerCase();

        var ex = new PremiumException[1];

        String finalName = name;
        var result = userCache.get(name, x -> {
            for (int i = 0; i < fetchers.size(); i++) {
                var fetcher = fetchers.get(i);

                try {
                    return fetcher.apply(x);
                } catch (PremiumException e) {
                    if (i == 0 && e.getIssue() == PremiumException.Issue.UNDEFINED) {
                        ex[0] = e;
                        break;
                    }

                    if (i == fetchers.size() - 1) {
                        ex[0] = e;
                    }
                } catch (RuntimeException e) {
                    plugin.getLogger().debug("Unexpected exception while fetching premium user " + finalName, e);
                    if (i == fetchers.size() - 1) {
                        ex[0] = new PremiumException(PremiumException.Issue.UNDEFINED, e);
                    }
                }
            }
            return null;
        });

        if (ex[0] != null) {
            throw ex[0];
        }

        return result;
    }

    private PremiumUser getUserFromAshcon(String name) throws PremiumException {
        try {
            plugin.reportMainThread();
            var connection = (HttpURLConnection) new URL("https://api.ashcon.app/mojang/v2/user/" + name).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            switch (connection.getResponseCode()) {
                case 200 -> {
                    var data = AuthenticLibreLogin.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                    var uuid = data.get("uuid");
                    var username = data.get("username").getAsString();

                    return new PremiumUser(UUID.fromString(uuid.getAsString()), username, username.equalsIgnoreCase(name));
                }
                case 404 -> {
                    return null;
                }
                case 429 ->
                        throw new PremiumException(PremiumException.Issue.THROTTLED, GeneralUtil.readInput(connection.getErrorStream()));
                default ->
                        throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
            }
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, e);
        }
    }

    private PremiumUser getUserFromPlayerDB(String name) throws PremiumException {
        try {
            plugin.reportMainThread();
            var connection = (HttpURLConnection) new URL("https://playerdb.co/api/player/minecraft/" + name).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            switch (connection.getResponseCode()) {
                case 200 -> {
                    var data = AuthenticLibreLogin.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                    var id = data.get("data").getAsJsonObject().get("player").getAsJsonObject().get("id").getAsString();
                    var username = data.get("data").getAsJsonObject().get("player").getAsJsonObject().get("username").getAsString();

                    return new PremiumUser(
                            UUID.fromString(id),
                            username,
                            username.equalsIgnoreCase(name)
                    );
                }
                case 400 -> {
                    return null;
                }
                default ->
                        throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
            }
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, e);
        }
    }

    private PremiumUser getUserFromMojang(String name) throws PremiumException {
        try {
            plugin.reportMainThread();
            var connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            return switch (connection.getResponseCode()) {
                case 429 ->
                        throw new PremiumException(PremiumException.Issue.THROTTLED, GeneralUtil.readInput(connection.getErrorStream()));
                case 403,401 -> throw new PremiumException(PremiumException.Issue.FORBIDDEN, GeneralUtil.readInput(connection.getErrorStream()));
                case 204, 404 -> null;

                case 200 -> {
                    var data = AuthenticLibreLogin.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                    var id = data.get("id").getAsString();
                    var demo = data.get("demo");

                    yield demo != null ? null : new PremiumUser(
                            GeneralUtil.fromUnDashedUUID(id),
                            data.get("name").getAsString(),
                            true // Mojang API is always authoritative
                    );
                }
                case 500 ->
                        throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, GeneralUtil.readInput(connection.getErrorStream()));
                default ->
                        throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
            };
        } catch (SocketTimeoutException te) {
            throw new PremiumException(PremiumException.Issue.THROTTLED, "Mojang API timed out");
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.UNDEFINED, e);
        }
    }

    @Override
    public PremiumUser getUserForUUID(UUID uuid) throws PremiumException {
        try {
            plugin.reportMainThread();
            var connection = (HttpURLConnection) new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid.toString()).openConnection();

            return switch (connection.getResponseCode()) {
                case 429 ->
                        throw new PremiumException(PremiumException.Issue.THROTTLED, GeneralUtil.readInput(connection.getErrorStream()));
                case 204, 404 -> null;
                case 200 -> {
                    var data = AuthenticLibreLogin.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                    var name = data.get("name").getAsString();

                    yield new PremiumUser(uuid, name, true); // Mojang API is always authoritative
                }
                case 500 ->
                        throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, GeneralUtil.readInput(connection.getErrorStream()));
                default ->
                        throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
            };
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.UNDEFINED, e);
        }
    }
}
