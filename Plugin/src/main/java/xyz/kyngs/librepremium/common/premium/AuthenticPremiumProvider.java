package xyz.kyngs.librepremium.common.premium;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import xyz.kyngs.easydb.scheduler.ThrowableFunction;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumProvider;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AuthenticPremiumProvider implements PremiumProvider {

    private final Cache<String, PremiumUser> userCache;
    private final List<ThrowableFunction<String, PremiumUser, PremiumException>> fetchers;

    public AuthenticPremiumProvider() {
        userCache = Caffeine.newBuilder()
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .build();

        fetchers = new ArrayList<>(3);

        fetchers.add(this::getUserFromCloudProtected);
        fetchers.add(this::getUserFromMojang);
    }

    @Override
    public PremiumUser getUserForName(String name) throws PremiumException {
        name = name.toLowerCase();

        var ex = new PremiumException[1];

        var result = userCache.get(name, x -> {
            for (int i = 0; i < fetchers.size(); i++) {
                var fetcher = fetchers.get(i);

                try {
                    return fetcher.run(x);
                } catch (PremiumException e) {
                    if (i == fetchers.size() - 1) {
                        ex[0] = e;
                    }
                } catch (RuntimeException e) {
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

    private PremiumUser getUserFromCloudProtected(String name) throws PremiumException {
        try {
            var connection = (HttpURLConnection) new URL("https://mcapi.cloudprotected.net/uuid/" + name).openConnection();

            if (connection.getResponseCode() == 200) {
                var data = AuthenticLibrePremium.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                var arr = data.get("result").getAsJsonArray();

                var result = arr.get(0).getAsJsonObject();

                var uuid = result.get("uuid-formatted");
                var cached = result.get("cached").getAsBoolean();

                if (cached && uuid == null) throw new PremiumException(PremiumException.Issue.UNDEFINED, "Unsuitable");

                return uuid == null ? null : new PremiumUser(UUID.fromString(uuid.getAsString()), name);
            } else {
                throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
            }
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.UNDEFINED, e);
        }
    }

    private PremiumUser getUserFromMojang(String name) throws PremiumException {
        try {
            var connection = (HttpURLConnection) new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openConnection();

            return switch (connection.getResponseCode()) {
                case 429 ->
                        throw new PremiumException(PremiumException.Issue.THROTTLED, GeneralUtil.readInput(connection.getErrorStream()));
                case 204 -> null;
                case 400 ->
                        throw new PremiumException(PremiumException.Issue.UNDEFINED, GeneralUtil.readInput(connection.getErrorStream()));
                case 200 -> {
                    var data = AuthenticLibrePremium.GSON.fromJson(new InputStreamReader(connection.getInputStream()), JsonObject.class);

                    var id = data.get("id").getAsString();
                    var demo = data.get("demo");

                    yield demo != null ? null : new PremiumUser(
                            GeneralUtil.fromUnDashedUUID(id),
                            data.get("name").getAsString()
                    );
                }
                default ->
                        throw new PremiumException(PremiumException.Issue.SERVER_EXCEPTION, GeneralUtil.readInput(connection.getErrorStream()));
            };
        } catch (IOException e) {
            throw new PremiumException(PremiumException.Issue.UNDEFINED, e);
        }
    }
}
