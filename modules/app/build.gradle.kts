import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-library`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    api(project(":modules:domain"))
}
