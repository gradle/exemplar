plugins {
    kotlin("jvm") version "1.2.71"
    `java-gradle-plugin`
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

tasks.withType<Test> {
    useJUnitPlatform()
}

