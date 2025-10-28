package com.prevengos.plug.desktop.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider extends AutoCloseable {

    Connection getConnection() throws SQLException;

    @Override
    default void close() throws Exception {
        // default no-op
    }
}
