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
        ":apps:java:api-service:dockerBuild",
        ":apps:java:orchestration:dockerBuild",
        ":apps:java:policy-service:dockerBuild",
        ":apps:platform:ui-service:dockerBuild"
    )
}
