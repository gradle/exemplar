group = "org.gradle.samples"
version = "0.1.0"
description = "Conventional plugin that applies Exemplar to a project idiomatically"

plugins {
    `java-gradle-plugin`
    groovy
}

dependencies {
    implementation(project(":sample-discovery"))
    implementation(Libraries.TYPESAFE_CONFIG)
    testImplementation(Libraries.SPOCK_CORE)
}

gradlePlugin {
    plugins {
        create("exemplar") {
            id = "org.gradle.exemplar"
            implementationClass = "org.gradle.samples"
        }
    }
}
