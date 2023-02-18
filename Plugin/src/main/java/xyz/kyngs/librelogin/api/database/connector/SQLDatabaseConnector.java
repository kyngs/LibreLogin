package xyz.kyngs.librelogin.api.database.connector;

import java.sql.Connection;
import java.sql.SQLException;

public interface SQLDatabaseConnector extends DatabaseConnector<SQLException, Connection> {
}
