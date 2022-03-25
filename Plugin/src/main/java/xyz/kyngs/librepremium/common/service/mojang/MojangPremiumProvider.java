package xyz.kyngs.librepremium.common.service.mojang;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.gson.JsonObject;
import xyz.kyngs.librepremium.api.premium.PremiumException;
import xyz.kyngs.librepremium.api.premium.PremiumProvider;
import xyz.kyngs.librepremium.api.premium.PremiumUser;
import xyz.kyngs.librepremium.common.AuthenticLibrePremium;
import xyz.kyngs.librepremium.common.util.GeneralUtil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class MojangPremiumProvider implements PremiumProvider {

    private final Cache<String, PremiumUser> mojangCache;

    public MojangPremiumProvider() {
        mojangCache = Caffeine.newBuilder()
                .expireAfterWrite(20, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public PremiumUser getUserForName(String name) throws PremiumException {
        name = name.toLowerCase();

        var cached = mojangCache.getIfPresent(name);

        if (cached != null) return cached;

        var get = getUserInternally(name);

        if (get == null) return null;

        mojangCache.put(name, get);

        return get;
    }

    private PremiumUser getUserInternally(String name) throws PremiumException {
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
