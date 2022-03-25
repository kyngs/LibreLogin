package xyz.kyngs.librepremium.api.database;

import java.util.Collection;

public interface WriteDatabaseProvider {

    void saveUser(User user);

    void saveUsers(Collection<User> users);

}
