package com.prevengos.plug.desktop.db;

import com.prevengos.plug.desktop.config.DatabaseMode;
import com.prevengos.plug.desktop.config.DesktopConfiguration;

public final class ConnectionProviders {

    private ConnectionProviders() {
    }

    public static ConnectionProvider create(DesktopConfiguration configuration) {
        DatabaseMode mode = configuration.databaseMode();
        return switch (mode) {
            case LOCAL -> new SQLiteConnectionProvider(configuration.sqlitePath());
            case HUB -> new SqlServerConnectionProvider(
                    configuration.hubUrl(),
                    configuration.hubUsername(),
                    configuration.hubPassword()
            );
        };
    }
}
