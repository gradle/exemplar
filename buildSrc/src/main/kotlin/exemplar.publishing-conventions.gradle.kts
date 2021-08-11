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
    }
}

signing {
    sign(publishing.publications["mavenJava"])
    useInMemoryPgpKeys(System.getenv("PGP_SIGNING_KEY"), System.getenv("PGP_SIGNING_KEY_PASSPHRASE"))
}
