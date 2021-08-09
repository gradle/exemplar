plugins {
    id("maven-publish")
}

group = "org.gradle.exemplar"
version = rootProject.version

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = project.name
        from(components["java"])
    }
}
