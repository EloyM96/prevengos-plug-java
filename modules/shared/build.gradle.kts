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
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("com.networknt:json-schema-validator:1.0.88")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.0")
}

tasks.test {
    useJUnitPlatform()
}
