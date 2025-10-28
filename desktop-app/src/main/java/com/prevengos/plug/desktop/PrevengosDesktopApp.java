package com.prevengos.plug.desktop;

import com.prevengos.plug.desktop.config.DesktopConfiguration;
import com.prevengos.plug.desktop.config.ConfigurationException;
import com.prevengos.plug.desktop.ui.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class PrevengosDesktopApp extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        DesktopConfiguration configuration;
        try {
            configuration = DesktopConfiguration.fromSystemProperties();
        } catch (ConfigurationException e) {
            throw new IOException("No fue posible cargar la configuración de la aplicación", e);
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/main-view.fxml"));
        loader.setControllerFactory(param -> {
            if (param == MainController.class) {
                return new MainController(configuration);
            }
            try {
                return param.getConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalStateException("No se pudo crear el controlador " + param.getName(), e);
            }
        });

        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Prevengos Desktop");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
