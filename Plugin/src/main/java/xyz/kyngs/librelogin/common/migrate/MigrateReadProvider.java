package xyz.kyngs.librelogin.common.migrate;

import xyz.kyngs.librelogin.api.database.ReadDatabaseProvider;
import xyz.kyngs.librelogin.api.database.User;

import java.util.Collection;
import java.util.UUID;

public abstract class MigrateReadProvider implements ReadDatabaseProvider {

    @Override
    public User getByName(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public User getByPremiumUUID(UUID uuid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> getByIP(String ip) {
        throw new UnsupportedOperationException();
    }

}
