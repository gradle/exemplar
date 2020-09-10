plugins {
    id("maven-publish")
}

group = "org.gradle"
version = rootProject.version

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = project.name
        from(components["java"])
    }

    repositories {
        maven(url = "https://repo.gradle.org/gradle/ext-releases-local") {
            authentication {
                credentials {
                    fun stringProperty(name: String): String? = project.findProperty(name) as? String

                    username = stringProperty("artifactory_user") ?: "nouser"
                    password = stringProperty("artifactory_password") ?: "nopass"
                }
            }
        }
    }
}
