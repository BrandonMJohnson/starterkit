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

include(":apps")
project(":apps").projectDir = file("../apps")

include(":apps:java")
project(":apps:java").projectDir = file("../apps/java")

include(":apps:platform")
project(":apps:platform").projectDir = file("../apps/platform")

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

include(":apps:java:api-service")
project(":apps:java:api-service").projectDir = file("../apps/java/api-service")

include(":apps:java:orchestration")
project(":apps:java:orchestration").projectDir = file("../apps/java/orchestration")

include(":apps:java:policy-service")
project(":apps:java:policy-service").projectDir = file("../apps/java/policy-service")

include(":apps:platform:ui-service")
project(":apps:platform:ui-service").projectDir = file("../apps/platform/ui-service")
