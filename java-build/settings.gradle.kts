pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id("io.micronaut.application") version providers.gradleProperty("micronautGradlePluginVersion").get()
        id("io.micronaut.library") version providers.gradleProperty("micronautGradlePluginVersion").get()
    }
}

rootProject.name = "starterkit-java-build"

include(":services")
project(":services").projectDir = file("../services")

include(":services:java")
project(":services:java").projectDir = file("../services/java")

include(":services:platform")
project(":services:platform").projectDir = file("../services/platform")

include(":libs")
project(":libs").projectDir = file("../libs")

include(":libs:java")
project(":libs:java").projectDir = file("../libs/java")

include(":libs:java:commons")
project(":libs:java:commons").projectDir = file("../libs/java/commons")

include(":libs:java:persistence")
project(":libs:java:persistence").projectDir = file("../libs/java/persistence")

include(":libs:java:orchestration-clients")
project(":libs:java:orchestration-clients").projectDir = file("../libs/java/orchestration-clients")

include(":services:java:api-service")
project(":services:java:api-service").projectDir = file("../services/java/api-service")

include(":services:java:orchestration")
project(":services:java:orchestration").projectDir = file("../services/java/orchestration")

include(":services:java:policy-service")
project(":services:java:policy-service").projectDir = file("../services/java/policy-service")

include(":services:platform:ui-service")
project(":services:platform:ui-service").projectDir = file("../services/platform/ui-service")
