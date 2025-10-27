import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

application {
    mainClass.set("com.prevengos.plug.desktop.PrevengosDesktopApp")
}

javafx {
    version = "21.0.2"
    modules("javafx.controls", "javafx.fxml")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(project(":modules:shared"))
    implementation(project(":modules:gateway"))
    implementation("org.openjfx:javafx-controls:21.0.2")
    implementation("org.openjfx:javafx-fxml:21.0.2")
}
