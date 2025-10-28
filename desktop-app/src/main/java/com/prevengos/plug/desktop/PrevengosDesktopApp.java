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
import com.prevengos.plug.desktop.service.http.HttpRemoteSyncGateway;
import com.prevengos.plug.desktop.ui.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Punto de entrada principal de la aplicación de escritorio Prevengos Plug.
 */
public class PrevengosDesktopApp extends Application {

    private DatabaseManager databaseManager;

    @Override
    public void start(Stage primaryStage) {
        AppConfig config = AppConfig.load();
        Path databasePath = config.resolveDatabasePath();
        this.databaseManager = new DatabaseManager(databasePath);

        PatientRepository patientRepository = new PatientRepository(databaseManager);
        QuestionnaireRepository questionnaireRepository = new QuestionnaireRepository(databaseManager);
        MetadataRepository metadataRepository = new MetadataRepository(databaseManager);
        SyncEventRepository syncEventRepository = new SyncEventRepository(databaseManager);

        LocalStorageService localStorageService = new LocalStorageService(
                patientRepository,
                questionnaireRepository,
                metadataRepository,
                syncEventRepository
        );

        RemoteSyncGateway remoteSyncGateway = new HttpRemoteSyncGateway(config);
        ManualTransferService manualTransferService = new ManualTransferService(localStorageService, config.objectMapper());
        SyncService syncService = new SyncService(localStorageService, remoteSyncGateway, config);

        MainView mainView = new MainView(localStorageService, syncService, manualTransferService, config);

        Scene scene = new Scene(mainView.getRoot(), 1280, 720);
        primaryStage.setTitle("Prevengos Plug Desktop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
