package com.prevengos.plug.desktop.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public final class SqlServerConnectionProvider implements ConnectionProvider {

    private final String url;
    private final String username;
    private final String password;

    public SqlServerConnectionProvider(String url, String username, String password) {
        this.url = Objects.requireNonNull(url, "url");
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (username != null && !username.isBlank()) {
            return DriverManager.getConnection(url, username, password);
        }
        return DriverManager.getConnection(url);
    }
}
