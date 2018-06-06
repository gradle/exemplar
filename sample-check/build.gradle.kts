plugins {
    groovy
    `java-library`
    `maven-publish`
}

dependencies {
    api(Libraries.JUNIT)
    compileOnly(Libraries.JSR305)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.COMMONS_LANG3)
    implementation("org.gradle:gradle-tooling-api:${gradle.gradle.gradleVersion}")
    implementation(project(":sample-discovery"))
    runtime(Libraries.SLF4J) {
        because("This allows use of composite build + dependency locking with gradle/gradle")
    }
    testCompile(Libraries.SPOCK_CORE)
}

// Add samples as inputs for testing
java.sourceSets {
    getByName("test").java.srcDirs("src/test/samples")
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
