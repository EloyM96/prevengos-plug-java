import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    application
    id("org.jetbrains.kotlin.jvm")
}

application {
    mainClass.set("com.prevengos.plug.desktop.MainKt")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":modules:shared"))
    implementation(project(":modules:gateway"))
}
