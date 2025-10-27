plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.gradle.python") version "0.10.1" apply false
}

allprojects {
    group = "com.prevengos.plug"
    version = "0.1.0-SNAPSHOT"

    repositories {
        google()
        mavenCentral()
    }
}
