plugins {
    id("java-library")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
    }
    jcenter()
}
