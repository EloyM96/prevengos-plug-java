package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.repository.DatabaseManager;
import com.prevengos.plug.desktop.repository.MetadataRepository;
import com.prevengos.plug.desktop.repository.PatientRepository;
import com.prevengos.plug.desktop.repository.QuestionnaireRepository;
import com.prevengos.plug.desktop.repository.SyncEventRepository;
import com.prevengos.plug.desktop.service.LocalStorageService;
import com.prevengos.plug.desktop.service.ManualTransferService;
import com.prevengos.plug.desktop.service.RemoteSyncGateway;
import com.prevengos.plug.desktop.service.SyncService;
import com.prevengos.plug.desktop.ui.MainView;
import com.prevengos.plug.shared.sync.dto.SyncPullResponse;
import com.prevengos.plug.shared.sync.dto.SyncPushRequest;
import com.prevengos.plug.shared.sync.dto.SyncPushResponse;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainViewTest extends ApplicationTest {

    private Path tempDb;
    private DatabaseManager databaseManager;

    @BeforeEach
    void configureDatabasePath() throws Exception {
        tempDb = Files.createTempFile("prevengos", ".db");
        System.setProperty("database.path", tempDb.toString());
    }

    @AfterEach
    void cleanup() throws Exception {
        System.clearProperty("database.path");
        if (databaseManager != null) {
            databaseManager.close();
        }
        if (tempDb != null) {
            Files.deleteIfExists(tempDb);
        }
    }

    @Override
    public void start(Stage stage) {
        AppConfig config = AppConfig.load();
        databaseManager = new DatabaseManager(config.resolveDatabasePath());
        LocalStorageService storage = new LocalStorageService(
                new PatientRepository(databaseManager),
                new QuestionnaireRepository(databaseManager),
                new MetadataRepository(databaseManager),
                new SyncEventRepository(databaseManager)
        );
        RemoteSyncGateway stubGateway = new RemoteSyncGateway() {
            @Override
            public SyncPushResponse push(SyncPushRequest request) {
                return new SyncPushResponse(request.pacientes().size(), request.cuestionarios().size(), 0L, List.of());
            }

            @Override
            public SyncPullResponse pull(Long syncToken, int limit) {
                return new SyncPullResponse(List.of(), List.of(), List.of(), syncToken != null ? syncToken : 0L);
            }
        };
        SyncService syncService = new SyncService(storage, stubGateway, config);
        ManualTransferService manualTransferService = new ManualTransferService(storage, config.objectMapper());

        MainView mainView = new MainView(storage, syncService, manualTransferService, config);
        stage.setScene(new Scene(mainView.getRoot(), 800, 600));
        stage.show();
    }

    @Test
    void rendersTabs() {
        TabPane tabPane = lookup(".tab-pane").query();
        assertNotNull(tabPane);
        assertEquals(3, tabPane.getTabs().size());
    }
}
