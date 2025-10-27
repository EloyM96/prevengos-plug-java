package com.prevengos.plug.desktop;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PrevengosDesktopApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-root");

        HBox topBar = buildTopBar();
        root.setTop(topBar);

        Label lastSyncValue = new Label("Nunca");
        VBox heroSection = buildHeroSection();

        TilePane metricCards = buildMetricCards(lastSyncValue);

        ObservableList<String> queueItems = FXCollections.observableArrayList(
            "Sincronización incremental • completada",
            "Exportación documental • programada",
            "Copias automáticas • 02:00"
        );

        VBox queueSection = buildQueueSection(queueItems);

        VBox content = new VBox(24, heroSection, metricCards, queueSection);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(32, 24, 48, 24));

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.getStyleClass().add("content-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1024, 720);
        scene.getStylesheets().add(
            PrevengosDesktopApp.class
                .getResource("/com/prevengos/plug/desktop/styles.css")
                .toExternalForm()
        );

        primaryStage.setTitle("Prevengos PRL");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(540);
        primaryStage.setMinHeight(560);
        primaryStage.show();

        content.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double width = newWidth == null ? 0 : newWidth.doubleValue();
            if (width < 640) {
                metricCards.setPrefColumns(1);
            } else if (width < 900) {
                metricCards.setPrefColumns(2);
            } else {
                metricCards.setPrefColumns(3);
            }
        });

        setupActions(heroSection, queueItems, lastSyncValue);
    }

    private void setupActions(VBox heroSection, ObservableList<String> queueItems, Label lastSyncValue) {
        Button syncButton = (Button) heroSection.lookup("#syncNowButton");
        Button scheduleButton = (Button) heroSection.lookup("#scheduleButton");

        if (syncButton != null) {
            syncButton.setOnAction(event -> {
                lastSyncValue.setText("En curso…");
                queueItems.add(0, "Sincronización manual • en curso");
                if (queueItems.size() > 5) {
                    queueItems.remove(queueItems.size() - 1);
                }
            });
        }

        if (scheduleButton != null) {
            scheduleButton.setOnAction(event -> {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Programación");
                alert.setHeaderText("Programar sincronización");
                alert.setContentText("Las automatizaciones estarán disponibles en la próxima iteración.");
                alert.showAndWait();
            });
        }
    }

    private HBox buildTopBar() {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(20, 32, 20, 32));

        Label brand = new Label("Prevengos Hub");
        brand.getStyleClass().add("brand");

        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        Button preferencesButton = new Button("Preferencias");
        preferencesButton.getStyleClass().add("ghost-button");
        preferencesButton.setOnAction(event -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Preferencias");
            alert.setHeaderText("Configuración");
            alert.setContentText("Gestiona la configuración avanzada desde tu instancia de Prevengos en la nube.");
            alert.showAndWait();
        });

        topBar.getChildren().addAll(brand, spacer, preferencesButton);
        return topBar;
    }

    private VBox buildHeroSection() {
        VBox heroSection = new VBox();
        heroSection.getStyleClass().add("hero-section");
        heroSection.setSpacing(18);
        heroSection.setAlignment(Pos.CENTER_LEFT);
        heroSection.setMaxWidth(960);

        Label title = new Label("Control centralizado de Prevengos");
        title.getStyleClass().add("hero-title");

        Label subtitle = new Label("Sincroniza datos críticos, automatiza respaldos y mantén tus equipos alineados desde un solo lugar.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("hero-subtitle");

        Button syncNowButton = new Button("Sincronizar ahora");
        syncNowButton.setId("syncNowButton");
        syncNowButton.getStyleClass().add("primary-button");

        Button scheduleButton = new Button("Programar sincronización");
        scheduleButton.setId("scheduleButton");
        scheduleButton.getStyleClass().add("ghost-button");

        HBox actions = new HBox(16, syncNowButton, scheduleButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        heroSection.getChildren().addAll(title, subtitle, actions);
        return heroSection;
    }

    private TilePane buildMetricCards(Label lastSyncValue) {
        TilePane metrics = new TilePane();
        metrics.getStyleClass().add("metric-cards");
        metrics.setHgap(18);
        metrics.setVgap(18);
        metrics.setPrefColumns(2);
        metrics.setMaxWidth(960);

        Label pendingValue = new Label("3 en cola");
        Label environmentValue = new Label("Operativo");

        metrics.getChildren().addAll(
            createMetricCard("Última sincronización", lastSyncValue, "Mantén tus datos actualizados para los equipos en campo."),
            createMetricCard("Registros pendientes", pendingValue, "Las tareas encoladas se procesarán automáticamente."),
            createMetricCard("Estado del servicio", environmentValue, "Monitoreo constante de la infraestructura híbrida.")
        );

        return metrics;
    }

    private VBox buildQueueSection(ObservableList<String> queueItems) {
        VBox queueSection = new VBox();
        queueSection.getStyleClass().add("queue-section");
        queueSection.setSpacing(16);
        queueSection.setAlignment(Pos.TOP_LEFT);
        queueSection.setMaxWidth(960);

        Label title = new Label("Cola de automatizaciones");
        title.getStyleClass().add("section-title");

        Label subtitle = new Label("Supervisa las tareas programadas y su prioridad desde esta consola.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("section-subtitle");

        ListView<String> queueList = new ListView<>(queueItems);
        queueList.getStyleClass().add("queue-list");
        queueList.setPrefHeight(220);
        queueList.setFocusTraversable(false);
        queueList.setMouseTransparent(true);

        queueSection.getChildren().addAll(title, subtitle, queueList);
        return queueSection;
    }

    private VBox createMetricCard(String title, Label valueLabel, String caption) {
        Label titleLabel = new Label(title.toUpperCase());
        titleLabel.getStyleClass().add("metric-title");

        valueLabel.getStyleClass().add("metric-value");

        Label captionLabel = new Label(caption);
        captionLabel.setWrapText(true);
        captionLabel.getStyleClass().add("metric-caption");

        VBox card = new VBox(10, titleLabel, valueLabel, captionLabel);
        card.getStyleClass().add("info-card");
        card.setMinWidth(240);
        card.setPrefWidth(280);
        return card;
    }

    public static void main(String[] args) {
        Application.launch(PrevengosDesktopApp.class, args);
    }
}

