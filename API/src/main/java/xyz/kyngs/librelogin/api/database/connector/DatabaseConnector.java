/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.api.database.connector;

import xyz.kyngs.librelogin.api.util.ThrowableConsumer;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;

/**
 * An interface used to connect to a database.
 *
 * @param <E> The common exception type thrown by the database.
 * @param <I> The interface type used to talk to the database. For example, {@link java.sql.Connection} for SQL databases.
 * @author kyngs
 */
public interface DatabaseConnector<E extends Exception, I> {

    /**
     * Connects to the database.
     *
     * @throws E If an error occurs during the connection.
     */
    void connect() throws E;

    /**
     * @return Whether the database is connected.
     */
    boolean connected();

    /**
     * Disconnects from the database.
     * <br>
     * <b>All registered connectors will be disconnected during plugin shutdown.</b>
     *
     * @throws E If an error occurs during the disconnection.
     */
    void disconnect() throws E;

    /**
     * Obtains an interface used to talk to the database. Remember to close it after use.
     *
     * @return The interface used to talk to the database.
     * @throws E                     If an error occurs during obtaining the interface.
     * @throws IllegalStateException If the database is not connected.
     */
    I obtainInterface() throws E, IllegalStateException;

    /**
     * Runs a query on the database and handles the exception.
     * <br>
     * <b>This method closes the interface after the function has been run</b>
     *
     * @param function The function to run.
     * @param <V>      The return type of the function.
     * @return The result of the function.
     * @throws IllegalStateException If the database is not connected.
     */
    <V> V runQuery(ThrowableFunction<I, V, E> function) throws IllegalStateException;

    /**
     * Runs a query on the database and handles the exception.
     * <br>
     * <b>This method closes the interface after the consumer has been run</b>
     *
     * @param consumer The consumer to run.
     * @throws IllegalStateException If the database is not connected.
     */
    default void runQuery(ThrowableConsumer<I, E> consumer) throws IllegalStateException {
        runQuery((i) -> {
            consumer.accept(i);
            return null;
        });
    }
}
