package com.prevengos.plug.desktop.config;

/**
 * Supported persistence modes for the desktop application.
 */
public enum DatabaseMode {
    LOCAL("SQLite local"),
    HUB("Hub SQL Server");

    private final String displayName;

    DatabaseMode(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
