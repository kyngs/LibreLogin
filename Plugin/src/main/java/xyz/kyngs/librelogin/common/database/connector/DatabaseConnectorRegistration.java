package xyz.kyngs.librelogin.common.database.connector;

import xyz.kyngs.librelogin.api.database.connector.DatabaseConnector;
import xyz.kyngs.librelogin.api.util.ThrowableFunction;

/**
 * This class is used to register new database connectors.
 *
 * @param <E>     The common exception type thrown by the database.
 * @param <C>     The type of the database connector.
 * @param factory The factory used to create the connector. The string parameter is the configuration prefix.
 * @param id
 */
public record DatabaseConnectorRegistration<E extends Exception, C extends DatabaseConnector<E, ?>>(
        ThrowableFunction<String, C, E> factory, Class<?> configClass, String id) {
}
