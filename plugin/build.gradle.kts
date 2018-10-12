plugins {
    kotlin("jvm") version "1.2.71"
    `java-gradle-plugin`
}

dependencies {
    implementation(gradleApi())
    implementation(kotlin("stdlib-jdk8"))
}

gradlePlugin {
    plugins {
        create("exemplar") {
            id = "org.gradle.exemplar"
            implementationClass = "org.gradle.exemplar.ExemplarPlugin"
        }
    }
}

