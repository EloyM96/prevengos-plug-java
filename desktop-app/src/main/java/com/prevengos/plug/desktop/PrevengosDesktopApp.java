package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.config.DatabaseMode;
import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.db.ConnectionProvider;
import com.prevengos.plug.desktop.db.ConnectionProviders;
import com.prevengos.plug.desktop.pacientes.JdbcPacienteRepository;
import com.prevengos.plug.desktop.pacientes.PacienteRepository;
import com.prevengos.plug.desktop.pacientes.PacientesPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public final class PrevengosDesktopApp extends Application {

    private DesktopConfiguration configuration;
    private ConnectionProvider connectionProvider;
    private ExecutorService executorService;
    private PacienteRepository pacienteRepository;
    private PacientesPane pacientesPane;

    @Override
    public void start(Stage primaryStage) {
        configuration = DesktopConfiguration.load();

        try {
            connectionProvider = ConnectionProviders.create(configuration);
        } catch (RuntimeException ex) {
            showFatalError("No se pudo inicializar la base de datos", ex);
            return;
        }

        pacienteRepository = new JdbcPacienteRepository(connectionProvider, configuration.databaseMode());
        executorService = Executors.newFixedThreadPool(2, new DesktopThreadFactory());

        pacientesPane = new PacientesPane(pacienteRepository, executorService);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");
        root.setTop(buildTopBar());
        root.setCenter(pacientesPane);
        root.setBottom(buildStatusBar());

        Scene scene = new Scene(root, 1280, 760);
        scene.getStylesheets().add(
                PrevengosDesktopApp.class
                        .getResource("/com/prevengos/plug/desktop/styles.css")
                        .toExternalForm()
        );

        primaryStage.setTitle("Prevengos PRL Desktop");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(980);
        primaryStage.setMinHeight(680);
        primaryStage.show();

        pacientesPane.refresh();
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox(18);
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(18, 28, 18, 28));

        Label brand = new Label("Prevengos Hub");
        brand.getStyleClass().add("brand");

        Label environmentLabel = new Label(configuration.environmentLabel());
        environmentLabel.getStyleClass().add("environment-label");

        Label modeLabel = new Label(configuration.databaseMode().displayName());
        modeLabel.getStyleClass().add("mode-label");

        VBox environmentBox = new VBox(4, environmentLabel, modeLabel);
        environmentBox.getStyleClass().add("environment-box");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Recargar datos");
        refreshButton.getStyleClass().add("ghost-button");
        refreshButton.setOnAction(event -> pacientesPane.refresh());

        topBar.getChildren().addAll(brand, environmentBox, spacer, refreshButton);
        return topBar;
    }

    private HBox buildStatusBar() {
        HBox statusBar = new HBox(12);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        statusBar.setPadding(new Insets(10, 28, 10, 28));

        Label connectionLabel = new Label(buildConnectionSummary());
        connectionLabel.getStyleClass().add("status-connection");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("status-message");
        statusLabel.textProperty().bind(pacientesPane.statusProperty());

        statusBar.getChildren().addAll(connectionLabel, spacer, statusLabel);
        return statusBar;
    }

    private String buildConnectionSummary() {
        if (configuration.databaseMode() == DatabaseMode.LOCAL) {
            return "SQLite · " + configuration.sqlitePath();
        }
        return "SQL Server · " + Optional.ofNullable(configuration.hubUrl()).orElse("(URL no definida)");
    }

    private void showFatalError(String message, Exception ex) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error crítico");
        alert.setHeaderText(message);
        alert.setContentText(Optional.ofNullable(ex.getMessage()).orElse(ex.toString()));
        alert.showAndWait();
        Platform.exit();
    }

    @Override
    public void stop() {
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (connectionProvider != null) {
            try {
                connectionProvider.close();
            } catch (Exception ignored) {
                // no-op
            }
        }
    }

    public static void main(String[] args) {
        Application.launch(PrevengosDesktopApp.class, args);
    }

    private static final class DesktopThreadFactory implements ThreadFactory {

        private static final AtomicInteger COUNTER = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "prevengos-desktop-" + COUNTER.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
