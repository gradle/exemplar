plugins {
    groovy
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly(Libraries.JSR305)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.TYPESAFE_CONFIG)
    testCompile(Libraries.SPOCK_CORE)
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = base.archivesBaseName
        from(components["java"])
    }

    repositories {
        val targetRepoKey = "libs-${buildTagFor(project.version as String)}s-local"
        maven(url = "https://repo.gradle.org/gradle/$targetRepoKey") {
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

fun buildTagFor(version: String): String =
        when (version.substringAfterLast('-')) {
            "SNAPSHOT" -> "snapshot"
            else -> "release"
        }
