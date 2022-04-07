package xyz.kyngs.librepremium.api.database;

import java.util.Collection;

public interface WriteDatabaseProvider {

    void insertUser(User user);

    void insertUsers(Collection<User> users);

    void updateUser(User user);

    void deleteUser(User user);

}
