plugins {
    id("maven-publish")
    signing
}

group = rootProject.group
version = rootProject.version

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = project.name
        from(components["java"])

        pom {
            name.set("Exemplar ${project.name}")
            description.set("Given a collection of sample projects, this library allows you to verify the samples' output.")
            url.set("https://github.com/gradle/exemplar")
            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                }
            }
            developers {
                developer {
                    name.set("The Gradle team")
                    organization.set("Gradle Inc.")
                    organizationUrl.set("https://gradle.com")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/gradle/exemplar.git")
                developerConnection.set("scm:git:ssh://git@github.com:gradle/exemplar.git")
                url.set("https://github.com/gradle/exemplar")
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
    useInMemoryPgpKeys(System.getenv("PGP_SIGNING_KEY"), System.getenv("PGP_SIGNING_KEY_PASSPHRASE"))
}
