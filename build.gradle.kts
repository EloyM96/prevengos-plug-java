plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.gradle.python") version "0.10.1" apply false
    id("com.google.cloud.tools.jib") version "3.4.1" apply false
}

allprojects {
    group = "com.prevengos.plug"
    version = "0.1.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("containerizeAll") {
    description = "Builds all container images for deployable modules"
    group = "distribution"
    dependsOn(":modules:api-rest:jibDockerBuild")
}
