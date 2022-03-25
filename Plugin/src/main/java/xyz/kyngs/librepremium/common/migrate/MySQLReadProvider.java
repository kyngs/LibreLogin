package xyz.kyngs.librepremium.common.migrate;

import xyz.kyngs.easydb.EasyDB;
import xyz.kyngs.easydb.provider.mysql.MySQL;
import xyz.kyngs.librepremium.api.Logger;
import xyz.kyngs.librepremium.api.database.ReadDatabaseProvider;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class MySQLReadProvider implements ReadDatabaseProvider {

    protected final EasyDB<MySQL, Connection, SQLException> easyDB;
    protected final String tableName;
    protected final Logger logger;

    protected MySQLReadProvider(EasyDB<MySQL, Connection, SQLException> easyDB, String tableName, Logger logger) {
        this.easyDB = easyDB;
        this.tableName = tableName;
        this.logger = logger;
    }
}
