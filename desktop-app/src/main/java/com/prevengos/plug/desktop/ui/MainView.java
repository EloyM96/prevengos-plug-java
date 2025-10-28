package com.prevengos.plug.desktop.ui;

import com.prevengos.plug.desktop.config.AppConfig;
import com.prevengos.plug.desktop.service.LocalStorageService;
import com.prevengos.plug.desktop.service.ManualTransferService;
import com.prevengos.plug.desktop.service.SyncService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * Construye la vista principal compuesta por pestañas de pacientes, cuestionarios y sincronización.
 */
public class MainView {

    private final BorderPane root;

    public MainView(LocalStorageService localStorageService,
                    SyncService syncService,
                    ManualTransferService manualTransferService,
                    AppConfig config) {
        TabPane tabPane = new TabPane();
        tabPane.getTabs().add(new Tab("Pacientes", new PatientManagementPane(localStorageService).getRoot()));
        tabPane.getTabs().add(new Tab("Cuestionarios", new QuestionnaireManagementPane(localStorageService).getRoot()));
        tabPane.getTabs().add(new Tab("Sincronización", new SyncPane(syncService, manualTransferService, config).getRoot()));
        tabPane.getTabs().forEach(tab -> tab.setClosable(false));

        root = new BorderPane();
        root.setCenter(tabPane);
        BorderPane.setMargin(tabPane, new Insets(10));
    }

    public BorderPane getRoot() {
        return root;
    }

    private static void showError(String title, Throwable throwable) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(throwable.getMessage());
        alert.showAndWait();
    }

    /**
     * Panel que muestra información de sincronización y permite ejecutar acciones manuales.
     */
    private static class SyncPane {
        private final BorderPane root;
        private final Label statusLabel;

        SyncPane(SyncService syncService, ManualTransferService manualTransferService, AppConfig config) {
            statusLabel = new Label("Sin sincronización ejecutada aún");
            Button pushButton = new Button("Enviar cambios");
            pushButton.setOnAction(event -> runAsync(() -> syncService.pushChanges(),
                    summary -> statusLabel.setText("Cambios enviados: " + summary.getEventsProcessed()),
                    throwable -> showError("Error enviando cambios", throwable)));

            Button pullButton = new Button("Recibir cambios");
            pullButton.setOnAction(event -> runAsync(() -> syncService.pullUpdates(),
                    summary -> statusLabel.setText("Eventos recibidos: " + summary.getEventsProcessed()),
                    throwable -> showError("Error recibiendo cambios", throwable)));

            Button exportButton = new Button("Exportar JSON");
            exportButton.setOnAction(event -> runAsync(() -> {
                        Path tmp = Path.of(System.getProperty("java.io.tmpdir"), "prevengos-export-" + Instant.now().toEpochMilli() + ".json");
                        manualTransferService.exportAll(tmp);
                        return tmp;
                    },
                    path -> statusLabel.setText("Exportado en " + path),
                    throwable -> showError("Error exportando", throwable)));

            Button importButton = new Button("Importar JSON");
            importButton.setOnAction(event -> runAsync(() -> {
                        Path importPath = Path.of(config.resolveDatabasePath().getParent().toString(), "import.json");
                        manualTransferService.importAll(importPath);
                        return importPath;
                    },
                    path -> statusLabel.setText("Importado desde " + path),
                    throwable -> showError("Error importando", throwable)));

            HBox actions = new HBox(10, pushButton, pullButton, exportButton, importButton);
            actions.setPadding(new Insets(10));
            actions.setSpacing(10);

            root = new BorderPane();
            root.setTop(actions);
            root.setCenter(statusLabel);
            BorderPane.setMargin(statusLabel, new Insets(10));
        }

        Node getRoot() {
            return root;
        }

        private <T> void runAsync(TaskSupplier<T> supplier,
                                  java.util.function.Consumer<T> onSuccess,
                                  java.util.function.Consumer<Throwable> onError) {
            CompletableFuture.supplyAsync(() -> {
                        try {
                            return supplier.get();
                        } catch (Throwable ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .whenComplete((result, throwable) -> Platform.runLater(() -> {
                        if (throwable != null) {
                            onError.accept(throwable.getCause() != null ? throwable.getCause() : throwable);
                        } else {
                            onSuccess.accept(result);
                        }
                    }));
        }
    }

    @FunctionalInterface
    private interface TaskSupplier<T> {
        T get() throws Exception;
    }
}
