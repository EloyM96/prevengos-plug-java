import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
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
    api(project(":modules:domain"))
}
