package xyz.kyngs.librelogin.api.database;

import java.util.Collection;

/**
 * This interface is used to write to the database.
 *
 * @author kyngs
 */
public interface WriteDatabaseProvider {

    /**
     * This method adds a player to the database.
     *
     * @param user The player to add.
     */
    void insertUser(User user);

    /**
     * This method adds several players to the database.
     *
     * @param users The players to add.
     */
    void insertUsers(Collection<User> users);

    /**
     * This method updates a player in the database.
     *
     * @param user The player to update.
     */
    void updateUser(User user);

    /**
     * This method deletes a player from the database.
     *
     * @param user The player to delete.
     */
    void deleteUser(User user);

}
