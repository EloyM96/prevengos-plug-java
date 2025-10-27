package com.prevengos.plug.desktop;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PrevengosDesktopApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        Label header = new Label("Prevengos Hub Desktop");
        Button syncButton = new Button("Sincronizar ahora");
        syncButton.setOnAction(event -> syncButton.setText("Sincronización encolada"));

        root.setTop(header);
        root.setCenter(syncButton);

        Scene scene = new Scene(root, 480.0, 320.0);
        primaryStage.setTitle("Prevengos PRL");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(PrevengosDesktopApp.class, args);
    }
}
