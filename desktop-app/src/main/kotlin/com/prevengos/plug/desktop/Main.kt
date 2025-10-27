package com.prevengos.plug.desktop

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.BorderPane
import javafx.stage.Stage

class PrevengosDesktopApp : Application() {
    override fun start(primaryStage: Stage) {
        val root = BorderPane()
        val header = Label("Prevengos Hub Desktop")
        val syncButton = Button("Sincronizar ahora").apply {
            setOnAction { text = "Sincronización encolada" }
        }
        root.top = header
        root.center = syncButton

        val scene = Scene(root, 480.0, 320.0)
        primaryStage.title = "Prevengos PRL"
        primaryStage.scene = scene
        primaryStage.show()
    }
}

fun main(args: Array<String>) {
    Application.launch(PrevengosDesktopApp::class.java, *args)
}
