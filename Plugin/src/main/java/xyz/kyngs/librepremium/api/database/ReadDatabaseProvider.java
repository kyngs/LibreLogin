package xyz.kyngs.librepremium.api.database;

import java.util.Collection;
import java.util.UUID;

public interface ReadDatabaseProvider {

    User getByName(String name);

    User getByUUID(UUID uuid);

    User getByPremiumUUID(UUID uuid);

    Collection<User> getAllUsers();

    default void invalidate(UUID uuid) {
        // NOOP
    }

}
