plugins {
    base
}

allprojects {
    group = "net.mudpot.starterkit"
    version = rootProject.property("projectVersion").toString()

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}

tasks.register("dockerBuild") {
    dependsOn(
        ":services:java:api-service:dockerBuild",
        ":services:java:orchestration:dockerBuild",
        ":services:java:policy-service:dockerBuild",
        ":services:platform:ui-service:dockerBuild"
    )
}
