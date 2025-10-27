import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    id("com.google.cloud.tools.jib")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
dependencies {
    implementation(project(":modules:app"))
    implementation(project(":modules:domain"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

val registry = providers.gradleProperty("containerRegistry").orNull
val imageName = providers.gradleProperty("containerImageName").orElse("api-rest").get()
val additionalTag = providers.gradleProperty("containerImageTag").orNull

jib {
    from {
        image = "eclipse-temurin:17-jre"
    }
    to {
        val targetRegistry = registry ?: "ghcr.io/prevengos"
        image = "$targetRegistry/$imageName"
        val tagsToPublish = mutableSetOf("latest", project.version.toString())
        additionalTag?.let { tagsToPublish.add(it) }
        tags = tagsToPublish
    }
    container {
        ports = listOf("8080")
        environment = mapOf(
            "SPRING_PROFILES_ACTIVE" to (providers.gradleProperty("springProfile").orElse("staging").get())
        )
        providers.gradleProperty("mainClassName").orNull?.let { mainClass.set(it) }
    }
}
