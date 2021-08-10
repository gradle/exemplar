plugins {
    id("maven-publish")
    signing
}

group = "org.gradle.exemplar"
version = rootProject.version

publishing {
    publications.create<MavenPublication>("mavenJava") {
        artifactId = project.name
        from(components["java"])
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

configure<SigningExtension> {
    useInMemoryPgpKeys(System.getenv("PGP_SIGNING_KEY"), System.getenv("PGP_SIGNING_KEY_PASSPHRASE"))
}