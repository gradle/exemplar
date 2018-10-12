plugins {
    kotlin("jvm") version "1.2.71"
    `java-gradle-plugin`
    id("com.gradle.plugin-publish") version "0.10.0"
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))

    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.3.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.3.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.3.1")
}

gradlePlugin {
    plugins {
        create("exemplar") {
            id = "org.gradle.exemplar"
            implementationClass = "org.gradle.exemplar.ExemplarPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/gradle/exemplar"
    vcsUrl = "https://github.com/gradle/exemplar"
    description = "Helps you to streamline the process of using Exemplar"
    tags = listOf("exemplar", "test", "testing", "samples", "sample-tests")

    plugins {
        getByName("exemplar") {
            displayName = "Exemplar plugin"
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

